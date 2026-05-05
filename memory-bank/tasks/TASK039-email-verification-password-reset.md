# TASK039 - Email Verification and Password Reset

Status: Completed

## Original Request
Add email sending with Thymeleaf templates and MailHog for dev. Send welcome email, verify account email with a unique 5 digit code that expires after 10 minutes and can be refreshed automatically or manually, and a forgot password email. Begin with backend, then mobile app. Match provided signup screens; do not implement biometric and do not include terms-of-service checkbox.

## Thought Process
Backend should own email security and code lifecycle. Verification and reset codes must expire, avoid plain storage, and not be logged. MailHog is only for local development. Mobile signup flow should move from one-step register to name/email/password -> code verify -> secure/preferences setup, while skipping biometric and terms checkbox per request.

## Implementation Plan
- Add backend mail/Thymeleaf dependencies, config, templates, MailHog compose/env docs.
- Add verification/reset code persistence with 5 digit unique active code and 10 minute expiry.
- Change registration to create unverified account and return pending verification.
- Add verify, resend verification, forgot password, reset password endpoints.
- Send welcome email after successful email verification.
- Update mobile Retrofit/auth repository/view model/screens for multi-step signup and forgot/reset flow.
- Verify backend package, Android compile, and docs.

## Subtasks
| Status | Item |
| --- | --- |
| Completed | Backend email, code, auth endpoints |
| Completed | Mobile signup/verification/forgot UI |
| Completed | Verification builds |
| Completed | Memory docs |

## Progress Log
- 2026-04-29: Started backend-first email verification/reset task. Defined contract: register returns pending verification, verify returns JWT and sends welcome, login auto-refreshes expired unverified code and blocks unverified users, resend manually refreshes code, forgot/reset use email codes.
- 2026-04-29: Added backend MailHog dev config, Spring Mail + Thymeleaf dependencies, email templates, user email_verified_at migration, auth_codes table, hash-only 5 digit code lifecycle, register/verify/resend/forgot/reset endpoints, welcome email after verification, and unverified login auto-refresh behavior.
- 2026-04-29: Updated Android auth API DTOs/repository/viewmodel. Register screen is now details -> 5 digit verify code. Login screen handles unverified account verification and forgot/reset password. Biometric and terms checkbox were intentionally not added.
- 2026-04-29: Reduced debug OkHttp logging from BODY to BASIC so passwords/codes are not logged. Verified with `sh mvnw -DskipTests package` and `./gradlew :app:compileDebugKotlin`.
