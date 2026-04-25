package com.serenade.backend.domain.rating.dto;

import java.util.UUID;

public record RatingResponse(UUID id, String targetType, UUID targetId, int value, double avg) {}
