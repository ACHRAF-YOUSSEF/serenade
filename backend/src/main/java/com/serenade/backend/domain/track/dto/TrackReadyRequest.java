package com.serenade.backend.domain.track.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TrackReadyRequest(
        @NotBlank String streamUrl,
        @Positive long durationMs
) {}
