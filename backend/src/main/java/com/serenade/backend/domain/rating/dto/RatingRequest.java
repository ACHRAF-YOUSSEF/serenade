package com.serenade.backend.domain.rating.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record RatingRequest(
        @NotBlank @Pattern(regexp = "TRACK|PLAYLIST") String targetType,
        @NotNull UUID targetId,
        @NotNull @Min(1) @Max(5) Integer value
) {}
