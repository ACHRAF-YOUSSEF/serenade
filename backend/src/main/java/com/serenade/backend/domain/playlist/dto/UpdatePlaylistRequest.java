package com.serenade.backend.domain.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdatePlaylistRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull Integer version
) {}
