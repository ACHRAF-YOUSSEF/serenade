package com.serenade.backend.domain.subtitle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubtitleLinePushRequest(
        @NotBlank String id,
        @NotNull @PositiveOrZero Long startMs,
        @NotNull @PositiveOrZero Long endMs,
        @NotBlank String text
) {}
