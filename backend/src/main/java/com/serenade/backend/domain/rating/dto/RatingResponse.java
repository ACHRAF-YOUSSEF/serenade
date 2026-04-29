package com.serenade.backend.domain.rating.dto;

import java.util.UUID;
import java.time.Instant;

public record RatingResponse(UUID id, String targetType, UUID targetId, int value, double avg, Instant updatedAt) {}
