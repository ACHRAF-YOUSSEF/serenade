package com.serenade.backend.domain.auth.dto;

import java.time.Instant;

public record RegistrationResponse(
        String userId,
        String email,
        boolean verificationRequired,
        Instant expiresAt) {}
