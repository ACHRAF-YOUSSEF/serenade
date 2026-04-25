package com.serenade.backend.domain.rating;

import com.serenade.backend.domain.rating.dto.RatingRequest;
import com.serenade.backend.domain.rating.dto.RatingResponse;
import com.serenade.backend.domain.playlist.Playlist;
import com.serenade.backend.domain.playlist.PlaylistRepository;
import com.serenade.backend.domain.track.Track;
import com.serenade.backend.domain.track.TrackRepository;
import com.serenade.backend.domain.user.User;
import com.serenade.backend.domain.user.UserRepository;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
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
    private final TrackRepository tracks;
    private final PlaylistRepository playlists;
    private final ReactiveStringRedisTemplate redis;

    public RatingService(RatingRepository ratings, UserRepository users, TrackRepository tracks,
                         PlaylistRepository playlists, ReactiveStringRedisTemplate redis) {
        this.ratings = ratings;
        this.users = users;
        this.tracks = tracks;
        this.playlists = playlists;
        this.redis = redis;
    }

    @Transactional
    public RatingResponse upsert(UUID userId, RatingRequest req) {
        if (req.value() == null || req.value() < 1 || req.value() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        assertRateableTarget(userId, req);
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
        String cached = readCache(key);
        if (cached != null) return Double.parseDouble(cached);
        return refreshAvgCache(targetType, targetId);
    }

    private double refreshAvgCache(String targetType, UUID targetId) {
        Double avg = ratings.findAvgByTargetTypeAndTargetId(targetType, targetId);
        double result = avg != null ? avg : 0.0;
        writeCache(redisKey(targetType, targetId), String.format("%.4f", result));
        return result;
    }

    private void assertRateableTarget(UUID userId, RatingRequest req) {
        switch (req.targetType()) {
            case "TRACK" -> {
                Track track = tracks.findById(req.targetId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                if (track.getUploader() != null && userId.equals(track.getUploader().getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            }
            case "PLAYLIST" -> {
                Playlist playlist = playlists.findById(req.targetId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                if (!playlist.getOwner().getId().equals(userId)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private String readCache(String key) {
        try {
            return redis.opsForValue().get(key).block(Duration.ofSeconds(1));
        } catch (RuntimeException _) {
            return null;
        }
    }

    private void writeCache(String key, String value) {
        try {
            redis.opsForValue().set(key, value, Duration.ofSeconds(60)).block(Duration.ofSeconds(1));
        } catch (RuntimeException _) {
            // Cache is an optimization; rating writes still succeed when Redis is unavailable.
        }
    }

    private static String redisKey(String targetType, UUID targetId) {
        return "rating:avg:" + targetType + ":" + targetId;
    }
}
