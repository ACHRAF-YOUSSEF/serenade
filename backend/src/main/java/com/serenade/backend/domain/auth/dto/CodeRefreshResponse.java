package com.serenade.backend.domain.auth.dto;

import java.time.Instant;

public record CodeRefreshResponse(String email, Instant expiresAt) {}
