package com.serenade.backend.domain.auth;

import com.serenade.backend.config.AppProperties;
import com.serenade.backend.domain.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthCodeService {

    private static final int CODE_BOUND = 100_000;

    private final AuthCodeRepository codes;
    private final AppProperties properties;
    private final SecureRandom random = new SecureRandom();

    public AuthCodeService(AuthCodeRepository codes, AppProperties properties) {
        this.codes = codes;
        this.properties = properties;
    }

    @Transactional
    public IssuedAuthCode issue(User user, AuthCodePurpose purpose) {
        return issue(user, user.getEmail(), purpose);
    }

    @Transactional
    public IssuedAuthCode issue(User user, String email, AuthCodePurpose purpose) {
        Instant now = Instant.now();
        String normalizedEmail = normalize(email);
        codes.consumeActiveForEmail(normalizedEmail, purpose, now);
        Instant expiresAt = now.plus(expiryMinutes(), ChronoUnit.MINUTES);
        String code = uniqueCode(purpose, now);
        codes.save(new AuthCode(user, normalizedEmail, purpose, hash(code), expiresAt));
        return new IssuedAuthCode(code, expiresAt);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveCode(String email, AuthCodePurpose purpose) {
        Instant now = Instant.now();
        return codes.findTopByEmailIgnoreCaseAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(normalize(email), purpose)
                .filter(code -> !code.isExpired(now))
                .isPresent();
    }

    @Transactional
    public void consume(String email, AuthCodePurpose purpose, String rawCode) {
        Instant now = Instant.now();
        AuthCode code = codes.findTopByEmailIgnoreCaseAndPurposeAndCodeHashAndConsumedAtIsNullOrderByCreatedAtDesc(
                        normalize(email),
                        purpose,
                        hash(rawCode))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));
        if (code.isExpired(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired");
        }
        code.consume(now);
    }

    private int expiryMinutes() {
        return properties.mail().verificationCodeExpiryMinutes();
    }

    private String uniqueCode(AuthCodePurpose purpose, Instant now) {
        for (int attempt = 0; attempt < 20; attempt++) {
            String candidate = "%05d".formatted(random.nextInt(CODE_BOUND));
            if (!codes.existsByPurposeAndCodeHashAndConsumedAtIsNullAndExpiresAtAfter(purpose, hash(candidate), now)) {
                return candidate;
            }
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to create verification code");
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private static String hash(String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(code.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append("%02x".formatted(b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
