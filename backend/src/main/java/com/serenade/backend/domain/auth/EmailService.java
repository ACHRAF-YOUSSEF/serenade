package com.serenade.backend.domain.auth;

import com.serenade.backend.config.AppProperties;
import com.serenade.backend.domain.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties properties;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, AppProperties properties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.properties = properties;
    }

    public void sendVerificationEmail(User user, String code, Instant expiresAt) {
        Context context = baseContext(user.getUsername());
        context.setVariable("code", code);
        context.setVariable("expiresAt", expiresAt);
        context.setVariable("expiresMinutes", properties.mail().verificationCodeExpiryMinutes());
        send(user.getEmail(), "Verify your Serenade account", "email/verify-account", context);
    }

    public void sendWelcomeEmail(User user) {
        Context context = baseContext(user.getUsername());
        send(user.getEmail(), "Welcome to Serenade", "email/welcome", context);
    }

    public void sendPasswordResetEmail(User user, String code, Instant expiresAt) {
        Context context = baseContext(user.getUsername());
        context.setVariable("code", code);
        context.setVariable("expiresAt", expiresAt);
        context.setVariable("expiresMinutes", properties.mail().verificationCodeExpiryMinutes());
        send(user.getEmail(), "Reset your Serenade password", "email/forgot-password", context);
    }

    private Context baseContext(String username) {
        Context context = new Context();
        context.setVariable("appName", properties.mail().appName());
        context.setVariable("username", username);
        context.setVariable("frontendBaseUrl", properties.mail().frontendBaseUrl());
        return context;
    }

    private void send(String to, String subject, String template, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(properties.mail().from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, context), true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Unable to build email message", ex);
        }
    }
}
