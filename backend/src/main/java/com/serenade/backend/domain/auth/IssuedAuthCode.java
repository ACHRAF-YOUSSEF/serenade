package com.serenade.backend.domain.auth;

import java.time.Instant;

public record IssuedAuthCode(String code, Instant expiresAt) {}
