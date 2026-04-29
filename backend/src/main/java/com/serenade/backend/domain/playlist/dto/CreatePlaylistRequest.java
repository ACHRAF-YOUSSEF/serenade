package com.serenade.backend.domain.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePlaylistRequest(@NotBlank @Size(max = 255) String name) {}
