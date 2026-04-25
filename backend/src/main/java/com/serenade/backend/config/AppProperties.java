package com.serenade.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Minio minio) {

    public record Jwt(String secret, long accessTokenExpiryMs, long refreshTokenExpiryMs) {}

    public record Minio(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucket,
            int presignedUrlExpiryMinutes) {}
}
