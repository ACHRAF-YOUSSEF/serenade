# Serenade — Music Streaming App

Full-stack music streaming platform. Anonymous browse + playback, auth-gated uploads/playlists/ratings, offline-first Android client, async transcode/subtitle pipeline.

## Stack

| Layer | Tech |
|---|---|
| Backend | Spring Boot 4, Java 25, PostgreSQL 16, Redis 7, RabbitMQ 3, MinIO |
| Workers | Python 3.12, FastAPI, aio-pika, FFmpeg, faster-whisper |
| Android | Kotlin, Jetpack Compose, Material 3, Room, Hilt, Media3, WorkManager |
| Infra | Docker Compose |

## Repo Layout

```
backend/          ← Spring Boot REST API
workers/
  shared/         ← shared Python utilities (SpringClient, MinIO, RabbitMQ, models)
  transcoder/     ← FFmpeg HLS pipeline worker
  subtitler/      ← faster-whisper → WebVTT subtitle worker
app/              ← Android app (:app module)
memory-bank/      ← project memory (read before contributing)
```

## Features

- **Browse & play** — no sign-in required
- **Search** — full-text + genre filter, PostgreSQL GIN index + `ts_rank`
- **Auth** — JWT (RS256/HS256), BCrypt, biometric gate on Android
- **Playlists** — create, copy, add/remove tracks, sync via outbox
- **Ratings** — per-track star rating, Redis-cached averages, self-rating blocked
- **Uploads** — multipart upload, async RabbitMQ pipeline, status polling
- **Transcoding** — FFmpeg → HLS (`.m3u8` + segments) stored in MinIO
- **Subtitles** — faster-whisper → WebVTT, synced overlay in player
- **Downloads** — offline playback via WorkManager + app-private storage
- **Sync** — cursor-based `GET /api/changes`, periodic WorkManager sync
- **Outbox** — offline mutations (playlist/rating) queue in Room, flush on sync

## Architecture

```
[Android App]  ──JWT──►  [Spring Boot API]  ──X-Api-Key──►  [Workers (FastAPI)]
                                │                                    │
                           [PostgreSQL]                         [RabbitMQ]
                           [Redis]                              [MinIO]
                           [MinIO]
```

- Mobile → Spring Boot: JWT auth (untrusted client)
- Spring Boot → Workers: `X-Api-Key` (internal only, never JWT)
- Workers → MinIO/RabbitMQ: credentials from env only

## Quick Start

### Infrastructure

```bash
docker compose -f backend/compose.yml up -d
```

Starts: PostgreSQL 16, Redis 7, RabbitMQ 3 (+ management UI), MinIO.

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Requires env vars:

```
JWT_SECRET=<256-bit random base64>
INTERNAL_API_KEY=<32+ byte random base64url>
POSTGRES_URL=jdbc:postgresql://localhost:5432/serenade
REDIS_URL=redis://localhost:6379
RABBITMQ_URL=amqp://localhost:5672
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=...
MINIO_SECRET_KEY=...
```

### Workers

```bash
# Transcoder
cd workers/transcoder
pip install -r requirements.txt
WORKER_API_KEY=<key> SPRING_BASE_URL=http://localhost:8080 python main.py

# Subtitler
cd workers/subtitler
pip install -r requirements.txt
WORKER_API_KEY=<key> SPRING_BASE_URL=http://localhost:8080 WHISPER_MODEL=base python main.py
```

Worker admin APIs:
- Transcoder: `http://localhost:8000/health`, `/metrics`, `POST /admin/reprocess/{track_id}`
- Subtitler: `http://localhost:8001/health`, `/metrics`, `POST /admin/reprocess/{track_id}`

### Android

```bash
cd app
./gradlew assembleDebug
```

Default API base: `http://10.0.2.2:8080/` (Android emulator). Adjust `BuildConfig.API_BASE_URL` for physical device.

Run unit tests:

```bash
./gradlew --gradle-user-home ../.gradle-user-home testDebugUnitTest
```

Run backend tests:

```bash
cd backend && sh mvnw test
```

## Security

Hard rules — no exceptions:

1. No secrets in source code, ever
2. `/internal/**` endpoints: `X-Api-Key` only, never JWT
3. FFmpeg subprocess: explicit arg list, `shell=False`
4. Worker API key: env only, never logged
5. Android release: `allowBackup="false"`, `debuggable="false"`
6. MinIO presigned URLs: ≤15 min expiry
7. JWT `alg: none` rejected unconditionally

See [`.github/instructions/security-auditor.instructions.md`](.github/instructions/security-auditor.instructions.md) for full OWASP audit checklist and pentest playbook.

## Project Memory

`memory-bank/` contains the persistent project context used across sessions:

| File | Purpose |
|---|---|
| `projectbrief.md` | Scope + goals |
| `productContext.md` | Why it exists, UX goals |
| `activeContext.md` | Current focus + next steps |
| `systemPatterns.md` | Architecture + patterns |
| `techContext.md` | Tech stack + constraints |
| `progress.md` | What works, what's left |
| `tasks/_index.md` | All tasks + statuses |

## Known Limitations

- Android module layout flat inside `:app` (no `:feature:*` split yet)
- `nowPlayingTrack` in AppNavigation is local state — no queue/playlist support
- Full HLS package download (`.m3u8` manifests) still pending
- Upload outbox (offline queue) still pending
- Playlist drag-reorder UI pending
- Workers `shared/` uses `sys.path` insert — use proper package install in prod
