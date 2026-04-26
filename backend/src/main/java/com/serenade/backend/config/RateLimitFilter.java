package com.serenade.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final RatePolicy LOGIN_POLICY = new RatePolicy("auth-login", 5, Duration.ofMinutes(1));
    private static final RatePolicy REGISTER_POLICY = new RatePolicy("auth-register", 3, Duration.ofHours(1));
    private static final RatePolicy AUTH_POLICY = new RatePolicy("auth", 10, Duration.ofMinutes(1));
    private static final RatePolicy API_POLICY = new RatePolicy("api", 60, Duration.ofMinutes(1));

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AppProperties props;

    public RateLimitFilter(AppProperties props) {
        this.props = props;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var request = (HttpServletRequest) req;
        var response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        RatePolicy policy = policyFor(path);
        if (policy == null) {
            chain.doFilter(req, res);
            return;
        }

        String key = resolveIp(request) + ":" + policy.name();
        Bucket bucket = buckets.computeIfAbsent(key, _ -> buildBucket(policy));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", Long.toString(probe.getRemainingTokens()));
            chain.doFilter(req, res);
        } else {
            long retryAfterSeconds = retryAfterSeconds(probe);
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
            response.getWriter().write("{\"error\":\"Too many requests\"}");
        }
    }

    private Bucket buildBucket(RatePolicy policy) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(policy.capacity())
                        .refillGreedy(policy.capacity(), policy.refillPeriod())
                        .build())
                .build();
    }

    private static RatePolicy policyFor(String path) {
        if (!path.startsWith("/api/")) {
            return null;
        }
        if (path.equals("/api/auth/login")) {
            return LOGIN_POLICY;
        }
        if (path.equals("/api/auth/register")) {
            return REGISTER_POLICY;
        }
        if (path.startsWith("/api/auth/")) {
            return AUTH_POLICY;
        }
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

    private static long retryAfterSeconds(ConsumptionProbe probe) {
        long nanos = probe.getNanosToWaitForRefill();
        return Math.max(1, (nanos + 999_999_999L) / 1_000_000_000L);
    }

    private record RatePolicy(String name, long capacity, Duration refillPeriod) {}
}
