# Music Streaming App

Full-stack music streaming platform.

## Stack

| Layer | Tech |
|---|---|
| Backend | Spring Boot 4, Java 25, Maven, PostgreSQL 16, Redis 7, RabbitMQ 3, MinIO |
| Workers | Python 3.12, FastAPI, aio-pika, faster-whisper, FFmpeg, MinIO async |
| Android | Kotlin, Jetpack Compose, Material 3, single-activity, feature modules |
| Infra | Docker Compose (local dev) |

## Repo Layout

```
backend/          ← Spring Boot API
workers/
  shared/         ← shared Python utilities
  transcoder/     ← FFmpeg HLS pipeline worker
  subtitler/      ← faster-whisper → WebVTT worker
app/              ← Android app
  app/            ← :app module
memory-bank/      ← project memory (read first!)
.github/
  instructions/   ← full agent instruction files
```

## Memory Bank

**Read all files in `memory-bank/` before starting any task.** This is the only persistent context across sessions. Key files:

- `memory-bank/projectbrief.md` — scope + goals
- `memory-bank/productContext.md` — why it exists, UX goals
- `memory-bank/activeContext.md` — current focus + next steps
- `memory-bank/systemPatterns.md` — architecture + patterns
- `memory-bank/techContext.md` — tech stack + constraints
- `memory-bank/progress.md` — what works, what's left
- `memory-bank/tasks/_index.md` — all tasks + statuses
- `memory-bank/tasks/TASKID-name.md` — individual task files

## Specialized Agents

Use slash commands to activate a specialized agent persona:

| Command | Scope | Purpose |
|---|---|---|
| `/worker` | `workers/**` | AI Worker Agent — Python/FastAPI/FFmpeg/Whisper |
| `/qa` | `**/test/**`, `**/*Test.java`, `**/test_*.py` | QA Tester — JUnit 5, pytest, Playwright |
| `/security` | `**` | Security Auditor — OWASP, pentest, code review |
| `/ui` | `**/ui/**`, `**/*Screen.kt`, `**/designsystem/**` | UI/UX Designer — Compose, Material 3 |
| `/memory-bank` | `**` | Memory Bank — read/update project memory |

Each command loads the full agent persona with domain rules, patterns, and non-negotiables.

## Security Non-Negotiables

These are absolute — no exceptions:

1. No secrets in source code (ever).
2. `/internal/**` endpoints: `X-Api-Key` only, never JWT.
3. FFmpeg subprocess: explicit arg list, `shell=False`.
4. Worker API key: env var only, never logged.
5. Android release: `allowBackup="false"`, `debuggable="false"`.
6. MinIO presigned URLs: ≤15 min expiry.
7. JWT `alg: none` rejected unconditionally.

## Common Commands

```bash
# Backend
cd backend && ./mvnw spring-boot:run
cd backend && ./mvnw test

# Android
cd app && ./gradlew assembleDebug
cd app && ./gradlew test

# Docker infra
docker compose up -d postgres redis rabbitmq minio
```
