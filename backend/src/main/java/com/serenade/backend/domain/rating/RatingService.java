package com.serenade.backend.domain.rating;

import com.serenade.backend.domain.rating.dto.RatingRequest;
import com.serenade.backend.domain.rating.dto.RatingResponse;
import com.serenade.backend.domain.user.User;
import com.serenade.backend.domain.user.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

@Service
public class RatingService {

    private final RatingRepository ratings;
    private final UserRepository users;
    private final StringRedisTemplate redis;

    public RatingService(RatingRepository ratings, UserRepository users, StringRedisTemplate redis) {
        this.ratings = ratings;
        this.users = users;
        this.redis = redis;
    }

    @Transactional
    public RatingResponse upsert(UUID userId, RatingRequest req) {
        User user = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Rating rating = ratings.findByUserIdAndTargetTypeAndTargetId(userId, req.targetType(), req.targetId())
                .orElseGet(() -> new Rating(user, req.targetType(), req.targetId(), req.value()));
        rating.setValue(req.value());
        ratings.save(rating);
        double avg = refreshAvgCache(req.targetType(), req.targetId());
        return new RatingResponse(rating.getId(), req.targetType(), req.targetId(), rating.getValue(), avg);
    }

    @Transactional(readOnly = true)
    public double getAvg(String targetType, UUID targetId) {
        String key = redisKey(targetType, targetId);
        String cached = redis.opsForValue().get(key);
        if (cached != null) return Double.parseDouble(cached);
        return refreshAvgCache(targetType, targetId);
    }

    private double refreshAvgCache(String targetType, UUID targetId) {
        Double avg = ratings.findAvgByTargetTypeAndTargetId(targetType, targetId);
        double result = avg != null ? avg : 0.0;
        redis.opsForValue().set(redisKey(targetType, targetId),
                String.format("%.4f", result), Duration.ofSeconds(60));
        return result;
    }

    private static String redisKey(String targetType, UUID targetId) {
        return "rating:avg:" + targetType + ":" + targetId;
    }
}
