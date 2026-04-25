package com.serenade.backend.domain.track.dto;

import java.util.UUID;

public record UploadResponse(UUID trackId, String status) {}
