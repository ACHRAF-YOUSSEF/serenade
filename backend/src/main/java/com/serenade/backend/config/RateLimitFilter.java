package com.serenade.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final RatePolicy LOGIN_POLICY    = new RatePolicy("auth-login",    5,  Duration.ofMinutes(1));
    private static final RatePolicy REGISTER_POLICY = new RatePolicy("auth-register", 3,  Duration.ofHours(1));
    private static final RatePolicy AUTH_POLICY     = new RatePolicy("auth",          10, Duration.ofMinutes(1));
    private static final RatePolicy API_POLICY      = new RatePolicy("api",           60, Duration.ofMinutes(1));

    private static final RedisScript<Long> INCR_EXPIRE_SCRIPT = RedisScript.of(
        "local n = redis.call('INCR', KEYS[1]) " +
        "if tonumber(n) == 1 then redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[1])) end " +
        "return n",
        Long.class
    );

    private final ReactiveStringRedisTemplate redisTemplate;
    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();
    private final AppProperties props;

    public RateLimitFilter(AppProperties props, ReactiveStringRedisTemplate redisTemplate) {
        this.props = props;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var request  = (HttpServletRequest)  req;
        var response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        RatePolicy policy = policyFor(path);
        if (policy == null) {
            chain.doFilter(req, res);
            return;
        }

        String ip    = resolveIp(request);
        long   count = consumeToken(ip, policy);

        if (count <= policy.capacity()) {
            response.setHeader("X-Rate-Limit-Remaining",
                Long.toString(Math.max(0, policy.capacity() - count)));
            chain.doFilter(req, res);
        } else {
            long nowMs       = System.currentTimeMillis();
            long windowMs    = policy.refillPeriod().toMillis();
            long windowEnd   = ((nowMs / windowMs) + 1) * windowMs;
            long retryAfter  = Math.max(1, (windowEnd - nowMs) / 1000);
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", Long.toString(retryAfter));
            response.getWriter().write("{\"error\":\"Too many requests\"}");
        }
    }

    private long consumeToken(String ip, RatePolicy policy) {
        long nowMs    = System.currentTimeMillis();
        long windowMs = policy.refillPeriod().toMillis();
        String key    = "rl:" + ip + ":" + policy.name() + ":" + (nowMs / windowMs);
        try {
            Long count = redisTemplate.execute(
                INCR_EXPIRE_SCRIPT,
                List.of(key),
                List.of(String.valueOf(windowMs * 2))
            ).blockFirst();
            return count != null ? count : 1L;
        } catch (Exception e) {
            log.warn("Redis rate-limit unavailable, falling back to local: {}", e.getMessage());
            String localKey = ip + ":" + policy.name();
            Bucket bucket = localBuckets.computeIfAbsent(localKey, _ -> buildLocalBucket(policy));
            return bucket.tryConsume(1) ? 1L : policy.capacity() + 1;
        }
    }

    private Bucket buildLocalBucket(RatePolicy policy) {
        return Bucket.builder()
            .addLimit(Bandwidth.builder()
                .capacity(policy.capacity())
                .refillGreedy(policy.capacity(), policy.refillPeriod())
                .build())
            .build();
    }

    private static RatePolicy policyFor(String path) {
        if (!path.startsWith("/api/")) return null;
        if (path.equals("/api/auth/login"))   return LOGIN_POLICY;
        if (path.equals("/api/auth/register")) return REGISTER_POLICY;
        if (path.startsWith("/api/auth/"))    return AUTH_POLICY;
        return API_POLICY;
    }

    private String resolveIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (isTrustedProxy(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }

    private boolean isTrustedProxy(String remoteAddr) {
        List<String> trusted = props.rateLimit().trustedProxies();
        return trusted != null && trusted.contains(remoteAddr);
    }

    private record RatePolicy(String name, long capacity, Duration refillPeriod) {}
}
