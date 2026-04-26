# Music Streaming App — Progress Tracker

Legend: `[ ]` todo · `[~]` in progress · `[x]` done · `[!]` blocked

Last updated: 2026-04-26

---

## Mobile (Android)

### M1 — Project Setup + Room DB Foundation
- [x] Create Android project (Empty Activity, Kotlin, API 26, Compose)
- [x] Add deps + enable KSP + Hilt plugin
- [ ] Create module layout (`:app`, `:core:*`, `:feature:*`)
- [x] Room entities (Track, Playlist, PlaylistTrackCrossRef, SubtitleLine, Download, PendingOp, Provider, User, Rating)
- [x] DAOs (Flow reads) for all entities
- [x] `AppDatabase` + type converters (Genre enum, Instant)
- [x] `Genre` enum

### M2 — Startup Auth Gate
- [ ] `AuthPreference` in DataStore
- [ ] `WelcomeScreen` (first-launch picker)
- [~] `AuthViewModel` (password auth wired; biometric still pending)
- [x] `SecureTokenStore` (Android Keystore AES-GCM; no tokens in Room)
- [ ] Biometric unlock via `CryptoObject` + Keystore AES
- [~] Auth guard on protected app routes/mutations

### M3 — Player Core (Media3)
- [x] `SerenadePlayerService : MediaSessionService`
- [x] Foreground service + `mediaPlayback` type + perms
- [ ] `MediaLibrarySession` for queue / seek / shuffle / repeat
- [x] `PlayerController` exposing playback `StateFlow`
- [x] Source resolver (local file first, stream URL fallback)
- [x] `PlayerScreen` + mini player
- [x] Persist playback queue + recently played history in Room

### M4 — Browse / Search / Genre Filter
- [ ] `HomeScreen` (featured, recent, trending)
- [~] `SearchScreen` (debounced query + genre chips; no tabs yet)
- [~] Offline fallback to Room (track list sync exists; search fallback still pending)
- [x] `SearchViewModel` with debounce + genre filter

### M5 — Playlists
- [x] `LibraryScreen`
- [x] `PlaylistDetailScreen` (view/play/copy/rate/add-remove/reorder done)
- [ ] Intelligent shuffle (`ShuffleOrder` custom impl)
- [x] Copy playlist flow (outbox-backed)
- [x] Rating (1-5 stars) + aggregate display (outbox-backed)

### M6 — Downloads + Offline
- [x] `DownloadScreen` / row button
- [~] `DownloadWorker` (WorkManager, expedited; full HLS package download pending)
- [x] Download complete notification (separate channel)
- [x] Delete download

### M7 — Upload (Mobile)
- [x] `UploadScreen` (SAF picker + metadata)
- [x] Multipart POST with OkHttp progress listener
- [x] Poll `GET /api/uploads/{id}` until READY/FAILED

### M8 — Subtitles Overlay
- [x] Fetch + in-memory cache subtitles
- [~] Cue display in `PlayerScreen` with 500 ms position flow
- [ ] "Generating…" banner + SSE wait

### M9 — Sync Strategy
- [x] `PendingOpEntity` outbox writes (playlist create/copy/add/remove/reorder, track/playlist rating, queued upload)
- [x] `SyncWorker` (periodic + on-connectivity)
- [x] `/changes?since=cursor` pull
- [ ] Conflict rules (ratings LWW, playlist version, track server-auth, idempotent UUIDs)
- [~] Outbox flush on login for anon → auth (periodic/pull flush exists; login trigger pending)

### M10 — Community Providers (Mobile)
- [ ] `MusicProvider` SPI in `:core:providers`
- [ ] `ProviderRegistry` + built-in `BackendProvider`
- [ ] `ProviderManifestLoader` (download + validate + sign check)
- [ ] `ManifestProvider` data-driven Retrofit
- [ ] `ProvidersScreen` (list / add / toggle / remove)
- [ ] OAuth via Chrome Custom Tabs
- [ ] Aggregated search across providers

---

## Backend (Spring Boot)

### B1 — Bootstrap
- [x] Generate project via Initializr
- [x] Manual pom deps (JJWT, MinIO, Bucket4j)
- [x] `application.yml` with dev/prod profiles
- [x] Docker Compose (Postgres 18-alpine3.23, Redis 7, RabbitMQ 3, MinIO)
- [x] Flyway `V1__init.sql` baseline
- [x] Actuator `/health` UP

### B2 — Auth (JWT)
- [x] `User` entity + repo
- [x] `POST /auth/register` (BCrypt + JWT pair)
- [x] `POST /auth/login`
- [x] `POST /auth/refresh`
- [x] `JwtAuthFilter`
- [x] `SecurityConfig` (stateless)
- [ ] Password policy validator
- [x] Rate limit `/auth/login` 5/min/IP

### B3 — Track Metadata + Streaming
- [x] `Track` entity + repo
- [x] `GET /api/tracks` + `GET /api/tracks/{id}`
- [~] HLS URL stored after worker ready callback
- [ ] Rate limit stream-start 100/min/user

### B4 — Search (Postgres FTS)
- [x] `tsvector` column + GIN index + trigger
- [x] `GET /api/search` with `plainto_tsquery` + `ts_rank`
- [~] Genre filter + page pagination (cursor pagination pending)

### B5 — Upload + Transcoding Pipeline
- [x] `POST /api/tracks/upload` multipart → MinIO + Track(PROCESSING) + rabbit publish
- [x] `GET /api/uploads/{id}` status
- [x] Transcoder worker (Python, Pydantic message validation, FFmpeg → HLS, SpringClient callback)
- [x] Worker callback updates Track.READY via `/internal/tracks/{id}/ready`
- [~] DLQ on processing failure (immediate dead-letter; retry/backoff pending)
- [x] Subtitler worker (Pydantic message validation + faster-whisper) consumes upload event in parallel
- [x] Worker callback persists subtitles via `/internal/tracks/{id}/subtitles`
- [ ] Idempotency table + header support

### B6 — Subtitles
- [x] `SubtitleLine` entity + repo
- [x] `GET /api/tracks/{id}/subtitles`
- [ ] SSE `/tracks/{id}/subtitles/stream` via Redis pub/sub

### B7 — Playlists + Copy + Rating
- [x] `Playlist` + `PlaylistTrack` entities
- [x] `Rating` entity (unique user/target)
- [x] CRUD endpoints (create, get, patch, tracks mgmt)
- [x] Copy playlist endpoint
- [x] Rating upsert + Redis-cached avg
- [~] Optimistic locking via `version` (rename uses version; track replacement version guard pending)

### B8 — Sync Pull
- [x] `GET /api/changes?since=&limit=`
- [~] Opaque cursor `(updatedAt)` (id tie-break pending)
- [~] Include version + updatedAt per entity (tracks/playlists/ratings updatedAt; playlist version included)
- [ ] Rate limit 60/min/user

### B9 — Rate Limiting + Security
- [~] Bucket4j filter for listed endpoints (in-memory; Redis-backed manager pending)
- [x] 429 + `Retry-After` header
- [x] CORS allow-list from YAML
- [x] Security headers (HSTS, nosniff, referrer-policy, CSP)
- [x] Upload size limits (50MB/60MB)
- [x] Presigned MinIO URLs 5-min expiry, clamped to 1–15 min
- [~] JWT/internal API keys from env (key-id rotation pending)
- [x] Trusted proxy handling for `X-Forwarded-For`
- [x] BCrypt strength 12
- [x] Swagger/OpenAPI protected by auth

### B10 — Observability
- [x] Actuator metrics/prometheus endpoints
- [x] Micrometer config
- [x] Structured JSON Logback
- [x] `X-Request-Id` filter → MDC + rabbit headers

---

## Infra / Ops

- [x] Docker Compose for local dev (Postgres, Redis, RabbitMQ, MinIO)
- [ ] Dockerfile for backend
- [ ] Dockerfile for transcoder worker (ffmpeg base image)
- [ ] Dockerfile for subtitler worker (python + faster-whisper)
- [ ] CI pipeline (build + test + lint)
- [ ] Secrets management strategy (env vars, not committed)

---

## Milestones

- [~] **MS1**: Backend B1–B3 + Mobile M1–M3 → core + persisted queue/history done; seeded HLS/device validation pending
- [~] **MS2**: Backend B4 + Mobile M4 → online search done; offline search fallback pending
- [~] **MS3**: Backend B7 + Mobile M5 → playlist CRUD/copy/rating usable; drag-reorder/add-remove UI pending
- [~] **MS4**: Mobile M6 → offline playback foundation done; HLS package download pending
- [~] **MS5**: Backend B5–B6 + Mobile M7–M8 → backend/workers/subtitle display done; mobile upload UI/SSE pending
- [~] **MS6**: Backend B8 + Mobile M9 → pull sync + playlist/rating outbox done; upload/reorder ops pending
- [ ] **MS7**: Mobile M10 → community providers
- [~] **MS8**: Backend B9–B10 → security/observability foundation done; Redis rate limiting + retry/backoff pending

---

## Notes / Blockers

- 2026-04-25: `memory-bank/` was missing; bootstrapped core memory files and TASK001.
- 2026-04-26: Collaboration docs tracking enabled: root markdown, `memory-bank`, `.github/instructions`, and `.claude` commands/skills are no longer ignored; `.claude/settings.local.json` remains ignored.
- 2026-04-25: User requested no tests. Do not add new test files for this task.
- 2026-04-25: Android Gradle needs repo-local Gradle user home in sandbox: `--gradle-user-home ../.gradle-user-home`.
- 2026-04-25: Backend package passed with tests skipped. Android `assembleDebug -x test` passed using repo-local Gradle home.
- 2026-04-25: Runtime health verified after Docker Compose was started: backend jar applied Flyway `V1__init.sql` and `GET /actuator/health` returned `{"groups":["liveness","readiness"],"status":"UP"}`.
- 2026-04-25: Applied Room review fixes: removed tokens from `UserEntity`, added `SecureTokenStore`, safe enum converters, dev destructive migration fallback, schema export, FK/indexes, playlist track JOIN, provider HTTPS repository guard, typed pending-op/provider capabilities, URL expiry field, and rating bounds. Android `assembleDebug -x test` passed.
- 2026-04-25: Replaced deprecated `EncryptedSharedPreferences`/`MasterKey` usage in `SecureTokenStore` with direct Android Keystore AES-GCM encryption and removed the unused `security-crypto` dependency. Android `assembleDebug -x test` passed.
- 2026-04-25: Fixed Android Hilt build failure by adding Retrofit providers for `PlaylistApiService` and `RatingApiService` in `NetworkModule`. Verified `:app:hiltJavaCompileDebug` and `assembleDebug -x test` pass.
- 2026-04-25: Completed playlist/rating phase: backend owner checks + playlist CRUD/copy/set-tracks + rating target validation + Redis avg cache; Android LibraryScreen + PlaylistDetailScreen + navigation/playback from playlist tracks. Verified `sh mvnw -DskipTests package` and Android `assembleDebug -x test`.
- 2026-04-25: Completed downloads/offline playback foundation: WorkManager `DownloadWorker`, `DownloadRepository`, row download/delete controls, `DownloadScreen`, local-file-first playback resolver, Room progress/state updates, and completion notification. Verified Android `assembleDebug -x test`. Limitation: worker downloads a single URL; full HLS package download remains pending for `.m3u8` manifests.
- 2026-04-25: Reviewed codebase and implemented sync phase: backend `GET /api/changes` for tracks/playlists/ratings with cursor; Android ChangesApiService, SyncRepository, Hilt SyncWorker, periodic scheduling, cursor persistence, and Room upserts. Verified `sh mvnw -DskipTests package` and Android `assembleDebug -x test`. Remaining fix: mutation repositories must emit PendingOpEntity rows for outbox flush.
- 2026-04-25: Reviewed sync/worker hardening and implemented outbox-backed playlist/rating mutations: PlaylistRepository writes Room + PendingOpEntity for create/copy, RatingRepository writes local rating + PendingOpEntity, and SyncRepository drains create/copy/rating ops. Hardened workers with Pydantic message validation, SpringClient callbacks, validated UUID-derived raw keys, and WORKER_API_KEY. Removed backend YAML secret defaults, added INTERNAL_API_KEY fail-fast, and redacted Android debug Authorization logs. Verified `sh mvnw -DskipTests clean package`, `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`, and `python3 -m compileall workers`.
- 2026-04-25: Reviewed upload phase and implemented backend/mobile M7 status flow: fixed backend raw object key to `raw/{trackId}` for worker compatibility, added uploader-scoped `GET /api/uploads/{id}`, added audio upload validation, and added Android SAF picker + metadata form + multipart progress + READY/FAILED polling. Verified `sh mvnw -DskipTests clean package`, `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`, `python3 -m compileall workers`, and `git diff --check`.
- 2026-04-25: Fixed worker admin port conflict after local startup test: transcoder admin API now uses `TRANSCODER_ADMIN_PORT` default `8000`, subtitler uses `SUBTITLER_ADMIN_PORT` default `8001`; updated worker `.env-example` and requirements, and settings now ignore extra local dotenv keys. Verified `python3 -m compileall workers/shared workers/transcoder workers/subtitler` and `git diff --check`.
- 2026-04-26: Reviewed B9/DLQ hardening. Fixed RabbitMQ DLQ wiring by adding dead-letter args on main worker queues and changed worker processing failures to no-requeue so messages dead-letter instead of looping. Hardened rate limiting with login/register policies, `Retry-After`, trusted-proxy `X-Forwarded-For`, YAML CORS allow-list, BCrypt strength 12, authenticated Swagger/OpenAPI, HSTS, and MinIO expiry clamp. Existing RabbitMQ queues need delete + redeclare to pick up DLX args.
- 2026-04-26: Reviewed B10 observability. Added backend `X-Request-Id` filter with MDC + response header + structured request completion logs, propagated request IDs into upload RabbitMQ headers and worker callbacks, switched backend console logs to Spring Boot logstash JSON, exposed authenticated `/actuator/metrics` and `/actuator/prometheus`, and added Micrometer app tags/histograms. Verified `sh mvnw -DskipTests package`, `python3 -m compileall workers/shared workers/transcoder workers/subtitler`, and `git diff --check`.
