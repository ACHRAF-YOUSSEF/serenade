package com.serenade.backend.domain.playlist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TrackPositionRequest(@NotNull UUID trackId, @NotNull @Min(0) Integer position) {}
