package com.serenade.backend.domain.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthCodeRepository extends JpaRepository<AuthCode, UUID> {

    boolean existsByPurposeAndCodeHashAndConsumedAtIsNullAndExpiresAtAfter(
            AuthCodePurpose purpose,
            String codeHash,
            Instant now);

    Optional<AuthCode> findTopByEmailIgnoreCaseAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String email,
            AuthCodePurpose purpose);

    Optional<AuthCode> findTopByEmailIgnoreCaseAndPurposeAndCodeHashAndConsumedAtIsNullOrderByCreatedAtDesc(
            String email,
            AuthCodePurpose purpose,
            String codeHash);

    @Modifying
    @Query("""
            update AuthCode c
            set c.consumedAt = :now
            where lower(c.email) = lower(:email)
              and c.purpose = :purpose
              and c.consumedAt is null
            """)
    void consumeActiveForEmail(
            @Param("email") String email,
            @Param("purpose") AuthCodePurpose purpose,
            @Param("now") Instant now);
}
