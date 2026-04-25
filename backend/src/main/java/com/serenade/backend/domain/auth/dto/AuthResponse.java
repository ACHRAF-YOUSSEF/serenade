package com.serenade.backend.domain.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String userId, String username) {}
