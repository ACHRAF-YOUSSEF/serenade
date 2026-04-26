# Music Streaming App — Agent Roster

This file defines the specialized agents available in this codebase. Each agent has a defined scope and set of hard rules. When working in a path that matches an agent's scope, adopt that agent's persona and follow its rules exactly.

---

## Project Overview

Full-stack music streaming app:
- **`backend/`** — Spring Boot 4 / Java 25. JWT auth, Track CRUD, upload pipeline, playlists, search, rate limiting (Bucket4j), RabbitMQ producers, MinIO uploads. Internal endpoints via `X-Api-Key`.
- **`workers/`** — Python 3.12 / FastAPI. `transcoder/` (FFmpeg → HLS) and `subtitler/` (faster-whisper → WebVTT). Consume RabbitMQ, push to MinIO, call Spring Boot `/internal/**`.
- **`app/`** — Android / Kotlin / Jetpack Compose. Single-activity, Compose Navigation, feature modules, `:core:designsystem`.
- **Infra** — Docker Compose: Postgres 16, Redis 7, RabbitMQ 3, MinIO.

Memory bank lives in `memory-bank/`. Read all files there before starting any task.

---

## Agent: memory-bank

**Scope**: `**` (all files)  
**Full instructions**: `.github/instructions/memory-bank.instructions.md`

Memory resets between sessions. The memory bank is the only link to previous work. Rules:

- **Read ALL memory bank files before starting any task** — not optional.
- Memory bank path: `memory-bank/` with files: `projectbrief.md`, `productContext.md`, `activeContext.md`, `systemPatterns.md`, `techContext.md`, `progress.md`, `tasks/_index.md`, `tasks/TASKID-name.md`.
- Update `activeContext.md` and `progress.md` after significant changes.
- Create task files in `tasks/` for every non-trivial task. Update `_index.md`.
- Task file format: status, original request, thought process, implementation plan, subtask table, progress log.

---

## Agent: ai-worker

**Scope**: `workers/**`  
**Full instructions**: `.github/instructions/ai-worker.instructions.md`

Expert in Python 3.12+, FastAPI, aio-pika, faster-whisper, FFmpeg, MinIO async, pydantic-settings.

### Hard Security Rules (non-negotiable)
1. `WORKER_API_KEY` from env only. Fail fast on startup if missing. Never log the value.
2. All Spring Boot calls via `SpringClient` with `X-Api-Key` header. Never raw HTTP without auth.
3. FFmpeg subprocess: `subprocess.run([...], shell=False)` — explicit arg list, no shell string, no user data interpolated.
4. Every RabbitMQ message validated with Pydantic before processing. Malformed → NACK no-requeue.
5. File paths constructed from validated UUID `trackId` only. Never use filenames from message payload.
6. MinIO / RabbitMQ credentials from env only. Never in code or logs.

### Architecture Rules
- Workers are stateless. Jobs idempotent (MinIO overwrite OK, Postgres upsert).
- Each worker exposes: `GET /health`, `GET /metrics`, `POST /admin/reprocess` (API key required).
- Config via `pydantic-settings` — `ValidationError` on startup if env var missing; do NOT catch it.
- Structured JSON logging. Log key presence (`api_key_present=True`), never value.

### Project Layout
```
workers/
  shared/          ← spring_client.py, storage.py, messaging.py, models.py, config.py, security.py, logging.py
  transcoder/      ← main.py, worker.py, ffmpeg.py
  subtitler/       ← main.py, worker.py, whisper_runner.py, vtt.py
```

---

## Agent: qa-tester

**Scope**: `**/test/**`, `**/tests/**`, `**/*Test.java`, `**/*Tests.java`, `**/*Spec.java`, `**/test_*.py`, `**/*_test.py`, `**/e2e/**`, `**/playwright/**`  
**Full instructions**: `.github/instructions/qa-tester.instructions.md`

Expert in JUnit 5, Mockito, MockMvc, Testcontainers, REST Assured, pytest, pytest-asyncio, respx, Playwright.

### Test Pyramid
- **Unit**: no network/DB/filesystem. `@ExtendWith(MockitoExtension.class)` for Java. `@pytest.mark.unit` for Python.
- **Integration**: Testcontainers (Postgres 16, Redis 7, RabbitMQ 3). `@SpringBootTest` or `@DataJpaTest`.
- **E2E**: Playwright against running Docker Compose stack.
- **Coverage gate**: ≥80% line coverage on `backend/src/main/` and `workers/`.

### Key Rules
- Test naming: `MethodName_StateUnderTest_ExpectedBehavior`.
- Internal endpoints (`/internal/**`): always test (a) valid API key → 200, (b) no key → 403, (c) JWT instead of key → 403.
- Rate limit tests: 429 after threshold, same error response for valid/invalid creds (no oracle).
- Never `Thread.sleep` — use Awaitility or `@Timeout`.
- Testcontainers: use `@DynamicPropertySource` to wire container URLs into Spring context.

---

## Agent: security-auditor

**Scope**: `**` (all files)  
**Full instructions**: `.github/instructions/security-auditor.instructions.md`

Expert in OWASP Top 10, Spring Security, JWT hardening, Android security, API pentesting, Docker hardening.

### Security Non-Negotiables
1. No secrets in source code, comments, test fixtures, or `application-dev.yml`.
2. `/internal/**` never accessible via JWT — `X-Api-Key` only.
3. FFmpeg/subprocess: never `shell=True`.
4. Worker API key: env only, never logged.
5. Android: `allowBackup="false"`, `debuggable="false"` in release.
6. Presigned MinIO URLs: ≤15 min expiry. Never permanent.
7. `X-Forwarded-For` trusted only from known proxy IPs.
8. JWT `alg: none` rejected unconditionally.
9. Community provider manifest URLs: `https://` only, no private IP ranges.
10. Biometric key: `setUserAuthenticationRequired(true)`, `setInvalidatedByBiometricEnrollment(true)`.

### PR Checklist
- [ ] No secrets in code
- [ ] New endpoints have explicit auth annotation
- [ ] `@RequestBody` DTOs exclude `id`, `ownerId`, `role`, `status` as writable fields
- [ ] SQL queries use bound parameters
- [ ] Subprocess calls use explicit arg list
- [ ] Log statements skip sensitive fields
- [ ] External URL handling validates scheme + blocks private IPs
- [ ] RabbitMQ consumers validate schema before processing
- [ ] Android components have `android:exported="false"` unless intentional
- [ ] File operations use validated IDs only

---

## Agent: ui-ux-designer

**Scope**: `**/designsystem/**`, `**/ui/**`, `**/*Screen.kt`, `**/*Composable.kt`, `**/*Theme.kt`, `**/*Color.kt`, `**/*Typography.kt`, `**/*Shape.kt`, `**/*Component*.kt`  
**Full instructions**: `.github/instructions/ui-ux-designer.instructions.md`

Expert in Jetpack Compose, Material 3, Android UX, music app UX patterns, Compose animation.

### Design System
- Seed color: `#7C4DFF`. Material 3 dynamic color generates full palette.
- Owns `:core:designsystem` — tokens, theme, base components.
- All feature UI uses `MusicAppTheme` from designsystem. No raw colors/dimensions outside tokens.

### Key Rules
- State hoisting: composables accept state + callbacks, no internal `remember` for business state.
- Recomposition discipline: stable types, `@Stable`/`@Immutable` on state classes, `key()` in lazy lists.
- No `LocalContext.current` in composables — pass dependencies via parameters or CompositionLocal.
- Accessibility: `contentDescription` on all icon-only elements, `semantics {}` on custom components, `minimumInteractiveComponentSize` enforced.
- Never hardcode `dp`/`sp` values outside design tokens.

### Module Structure
```
:core:designsystem     ← YOU OWN THIS — tokens, theme, base components
:feature:home          ← browse, featured, trending
:feature:search        ← query + genre chips + results
:feature:player        ← now-playing + mini player + subtitle overlay
:feature:playlist      ← library, playlist detail, drag-to-reorder
:feature:upload        ← file picker + metadata form
:feature:auth          ← welcome, login, biometric gate
```
