package com.serenade.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Minio minio, Cors cors, RateLimit rateLimit, Mail mail) {

    public record Jwt(String secret, long accessTokenExpiryMs, long refreshTokenExpiryMs) {
    }

    public record Minio(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucket,
            int presignedUrlExpiryMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record RateLimit(List<String> trustedProxies) {
    }

    public record Mail(
            String from,
            String appName,
            int verificationCodeExpiryMinutes
    ) {
    }
}
