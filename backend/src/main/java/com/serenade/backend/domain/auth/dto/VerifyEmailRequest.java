package com.serenade.backend.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyEmailRequest(
        @Email @NotBlank String email,
        @Pattern(regexp = "\\d{5}", message = "Code must be 5 digits") String code) {}
