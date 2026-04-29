package com.serenade.backend.domain.auth;

import com.serenade.backend.domain.auth.dto.*;
import com.serenade.backend.domain.user.User;
import com.serenade.backend.domain.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthCodeService codeService;
    private final EmailService emailService;

    public AuthService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthCodeService codeService,
            EmailService emailService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.codeService = codeService;
        this.emailService = emailService;
    }

    public RegistrationResponse register(RegisterRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (users.existsByUsername(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        User user = new User(req.username(), normalize(req.email()), passwordEncoder.encode(req.password()));
        users.save(user);
        IssuedAuthCode code = codeService.issue(user, AuthCodePurpose.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user, code.code(), code.expiresAt());
        return new RegistrationResponse(user.getId().toString(), user.getEmail(), true, code.expiresAt());
    }

    public AuthResponse verifyEmail(VerifyEmailRequest req) {
        User user = users.findByEmail(normalize(req.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));
        codeService.consume(user.getEmail(), AuthCodePurpose.EMAIL_VERIFICATION, req.code());
        user.markEmailVerified();
        users.save(user);
        emailService.sendWelcomeEmail(user);
        return toAuthResponse(user);
    }

    public CodeRefreshResponse resendVerification(ResendVerificationRequest req) {
        User user = users.findByEmail(normalize(req.email()))
                .orElse(null);
        if (user == null) {
            return new CodeRefreshResponse(normalize(req.email()), null);
        }
        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already verified");
        }
        IssuedAuthCode code = codeService.issue(user, AuthCodePurpose.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user, code.code(), code.expiresAt());
        return new CodeRefreshResponse(user.getEmail(), code.expiresAt());
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(normalize(req.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!user.isEmailVerified()) {
            refreshVerificationIfNeeded(user);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email verification required");
        }
        return toAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest req) {
        String token = req.refreshToken();
        if (!jwtService.isRefreshToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
        }
        var userId = jwtService.extractUserId(token);
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email verification required");
        }
        return toAuthResponse(user);
    }

    public void forgotPassword(ForgotPasswordRequest req) {
        users.findByEmail(normalize(req.email())).ifPresent(user -> {
            IssuedAuthCode code = codeService.issue(user, AuthCodePurpose.PASSWORD_RESET);
            emailService.sendPasswordResetEmail(user, code.code(), code.expiresAt());
        });
    }

    public void resetPassword(ResetPasswordRequest req) {
        User user = users.findByEmail(normalize(req.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code"));
        codeService.consume(user.getEmail(), AuthCodePurpose.PASSWORD_RESET, req.code());
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        users.save(user);
    }

    private void refreshVerificationIfNeeded(User user) {
        if (codeService.hasActiveCode(user.getEmail(), AuthCodePurpose.EMAIL_VERIFICATION)) {
            return;
        }
        IssuedAuthCode code = codeService.issue(user, AuthCodePurpose.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user, code.code(), code.expiresAt());
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user.getId()),
                jwtService.generateRefreshToken(user.getId()),
                user.getId().toString(),
                user.getUsername()
        );
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
