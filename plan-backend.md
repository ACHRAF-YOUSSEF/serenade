# Music Streaming App — Backend Plan (Spring Boot)

Status:

Tags: spring-boot, java, postgres, redis, rabbitmq, minio, ffmpeg, whisper

---

## MVP Scope (Backend)

Serve:

- Auth (register / login / refresh) with JWT
- Track metadata + HLS streaming URLs
- Search with genre filter (Postgres FTS)
- Upload ingestion → async transcode → HLS variants
- AI subtitle generation when missing
- Playlists CRUD + copy + rating
- Sync pull endpoint (`/changes?since=`)
- Rate limiting on abuse-prone endpoints

---

## Tech Stack

- Spring Boot 4.0.5 + **Java 25**
- Maven build, YAML config, Jar packaging
- Spring Web, Spring Security + JWT (stateless)
- Spring Data JPA
- **PostgreSQL** — metadata (users, tracks, playlists, ratings, providers). Relational + full-text search
- **S3 / MinIO** — audio blobs (original + transcoded variants)
- **Redis** — rate limiting, short-lived caches, refresh tokens, pub/sub
- **RabbitMQ** — job queue for transcoding + subtitle generation
- **FFmpeg** — transcoder worker (HLS + 128/256 kbps Opus/AAC)
- **Whisper (faster-whisper)** — AI subtitle/lyric alignment worker
- **Flyway** — migrations
- **Bucket4j** (Redis-backed) — rate limit filter

**Why queue + rate limiting**: upload/transcode CPU-heavy + unbounded duration → must be async so HTTP doesn't time out, workers scale horizontally. Rate limit on upload, stream-start, auth to stop abuse + protect transcode workers.

---

## Project Metadata (Spring Initializr)

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 4.0.5 |
| Group | `com.musicstream` |
| Artifact | `backend` |
| Package | `com.musicstream.backend` |
| Packaging | Jar |
| Java | 25 |
| Config | YAML |

**Initializr deps:** Spring Web, Spring Security, Spring Data JPA, PostgreSQL Driver, Spring Data Redis, Spring for RabbitMQ, Flyway Migration, Actuator, Validation, DevTools.

**Manual pom.xml additions:** `jjwt-api/impl/jackson` 0.12.6, `minio` 8.5.12, `bucket4j-core/redis` 8.10.1.

---

## Module Layout

```
backend/
  src/main/java/com/musicstream/backend/
    config/        (SecurityConfig, RedisConfig, RabbitConfig, MinioConfig)
    auth/          (controller, service, jwt filter, user details)
    user/
    track/
    playlist/
    rating/
    upload/        (controller + service, emits rabbit messages)
    subtitle/
    search/        (Postgres FTS queries)
    provider/      (manifest validation if registry endpoint added)
    sync/          (changes endpoint)
    ratelimit/     (Bucket4j filter)
    storage/       (Minio/S3 wrapper)
    messaging/     (rabbit producers + consumers if colocated)
  src/main/resources/
    application.yml
    db/migration/  (Flyway V1__init.sql ...)

workers/
  transcoder/      (separate Spring Boot JVM app, consumes transcode.requested)
  subtitler/       (separate Spring Boot JVM app, consumes subtitles.requested)
```

---

## Feature B1: Project Bootstrap

**Steps:**

1. Generate via Initializr with settings above
2. Add manual pom deps (JJWT, MinIO, Bucket4j)
3. `application.yml` with profiles: `dev`, `prod`
4. Docker Compose for local: Postgres 16, Redis 7, RabbitMQ 3, MinIO latest
5. Flyway `V1__init.sql` — schema bootstrap (empty tables defined later)
6. Health check via Actuator `/actuator/health`

**Done when:** `./mvnw spring-boot:run` → GET `/actuator/health` returns UP, all infra up via compose.

---

## Feature B2: Auth (JWT Stateless)

**Steps:**

1. Entities: `User(id: UUID, username, email, passwordHash, createdAt)`
   > **ID convention (all entities):** `@Id @UuidGenerator private UUID id;` (Hibernate 6 / Spring Boot 4). PostgreSQL column type `uuid` (16-byte native). Never `SERIAL`/`BIGSERIAL`. All FK references typed `UUID`.
2. `POST /auth/register` — BCrypt hash → insert → issue JWT pair
3. `POST /auth/login` — verify → issue access (15 min) + refresh (30 days) JWTs
4. `POST /auth/refresh` — validate refresh → rotate → new pair. Blacklist old refresh in Redis until expiry
5. `JwtAuthFilter` — parse Bearer token → set `SecurityContext`
6. `SecurityConfig` — stateless, CSRF disabled, permit `/auth/**` + `/actuator/health`, authenticated everything else
7. Password policy: min 8 chars, one digit, one letter (server-side `@Valid`)
8. Rate limit `/auth/login` — 5/min per IP via Bucket4j

**Done when:** register → login → protected endpoint works with Bearer; refresh rotates.

---

## Feature B3: Track Metadata + Streaming

**Steps:**

1. Entity: `Track(id: UUID, title, artist, album, genre, durationMs, artworkUrl, streamUrl, status ∈ {PROCESSING, READY, FAILED}, uploaderId: UUID, version: Int, updatedAt)`
2. `GET /tracks/{id}` — public read
3. HLS master playlist served directly from MinIO presigned URL OR streamed through backend with range support
4. Rate limit stream-start — 100/min per user

**Done when:** Seeded track → GET returns metadata → HLS URL plays in mobile.

---

## Feature B4: Search (Postgres FTS)

**Steps:**

1. `tsvector` column on `Track(title || artist || album)` with GIN index + trigger updating on insert/update
2. `GET /search?q=&genre=&page=&size=` — query with `plainto_tsquery`, rank by `ts_rank`, filter by genre enum
3. Return `{ tracks: [...], playlists: [...] }` combined response
4. Pagination: cursor-based via `(rank, id)`

**Done when:** Query returns ranked results, genre filter works, sub-100ms on 100k tracks.

---

## Feature B5: Upload + Transcoding Pipeline

**Steps:**

1. `POST /uploads` (multipart):

   - Auth required, rate limit 10/hour/user (Bucket4j Redis)
   - Stream blob to MinIO `raw/{uploadId}`
   - Insert `Track(status=PROCESSING)`
   - Publish RabbitMQ `transcode.requested { trackId, rawKey }`
   - Return `{ uploadId, trackId, status }`

2. `GET /uploads/{id}` — poll status; later expose SSE
3. **Transcode worker** (separate JVM):

   - Consumes `transcode.requested`
   - Pulls raw blob → FFmpeg → HLS segments (Opus 96k + AAC 128k + AAC 256k) → write `stream/{trackId}/`
   - Extract duration, replay-gain, waveform peaks
   - On success: publish `transcode.completed { trackId, masterUrl, durationMs }` → API updates `Track.status=READY`, `streamUrl`
   - On failure after N retries → DLQ

4. **Subtitle worker** (separate JVM):

   - On `transcode.completed` enqueue `subtitles.requested` if no lyrics
   - Worker: faster-whisper → insert `SubtitleLine(trackId, startMs, endMs, text)` rows → publish `subtitles.ready`

5. Idempotency: client-generated UUID in header, stored in `uploads_idempotency` table

**Done when:** Upload MP3 → 202 response → worker transcodes → track flips to READY → subtitles appear.

---

## Feature B6: Subtitles

**Steps:**

1. Entity: `SubtitleLine(id: UUID, trackId: UUID, startMs, endMs, text)`
2. `GET /tracks/{id}/subtitles` — ordered by `startMs`
3. SSE `/tracks/{id}/subtitles/stream` emits when `subtitles.ready` fires for that trackId (Redis pub/sub bridge)

**Done when:** Subtitle rows persisted; endpoint returns them; SSE pushes on completion.

---

## Feature B7: Playlists (CRUD + Copy + Rating)

**Steps:**

1. Entities:

   - `Playlist(id: UUID, name, ownerId: UUID, isCopy, sourcePlaylistId: UUID?, version: Int, updatedAt)`
   - `PlaylistTrack(playlistId: UUID, trackId: UUID, position: Int)` — composite PK
   - `Rating(id: UUID, userId: UUID, targetType ∈ {TRACK, PLAYLIST}, targetId: UUID, value 1–5, createdAt)` — unique `(userId, targetType, targetId)`

2. Endpoints:

   - `POST /playlists` — create (auth)
   - `GET /playlists/{id}` — public
   - `PATCH /playlists/{id}` — update (owner only, optimistic locking via `version`)
   - `POST /playlists/{id}/tracks` — add/remove/reorder (owner only)
   - `POST /playlists/{id}/copy` — clone metadata + tracks → new playlist owned by caller, `isCopy=true`, `sourcePlaylistId=original`
   - `POST /ratings` — upsert by `(userId, targetType, targetId)`; recompute + cache avg in Redis (TTL 60s)

3. Optimistic locking: client sends `version`; mismatch → 409 Conflict with current state

**Done when:** Create → edit → concurrent edit rejected → copy → rate → avg updates.

---

## Feature B8: Sync Pull Endpoint

**Steps:**

1. `GET /changes?since={cursor}&limit=500` — returns tracks + playlists + ratings changed since cursor
2. Cursor = opaque base64 of `(updatedAt, id)` of last item
3. Each entity carries `version` + `updatedAt` for client conflict resolution
4. Rate limit 60/min per user

**Done when:** Mobile sync worker drains changes correctly; idempotent on replay.

---

## Feature B9: Rate Limiting + Security Hardening

**Steps:**

1. `RateLimitFilter` (Bucket4j + Redis):

   - `/auth/login` — 5/min/IP
   - `/auth/register` — 3/hour/IP
   - `/uploads` — 10/hour/user
   - `/tracks/*/stream*` — 100/min/user
   - `/changes` — 60/min/user

2. Exceeded → 429 with `Retry-After` header
3. CORS: configurable allow-list via YAML
4. Security headers: HSTS, X-Content-Type-Options, Referrer-Policy via Spring Security
5. File upload limits: Spring `maxFileSize=50MB`, `maxRequestSize=60MB`
6. MinIO presigned URLs expire 5 min
7. JWT signing key from env var (never committed); rotate via key-id header

**Done when:** Bucket4j returns 429 on abuse; headers present on all responses.

---

## Feature B10: Observability

**Steps:**

1. Actuator: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`
2. Micrometer → Prometheus scrape
3. Structured JSON logging (Logback encoder)
4. Correlation ID filter: `X-Request-Id` propagated to MDC + downstream rabbit headers
5. Grafana dashboard (post-MVP optional)

**Done when:** Metrics exposed, logs structured, request IDs traceable end-to-end.

---

## Endpoint Summary

```
POST   /auth/register             (rate-limited)
POST   /auth/login                (rate-limited)
POST   /auth/refresh

GET    /tracks/{id}
GET    /tracks/{id}/subtitles
GET    /tracks/{id}/subtitles/stream   (SSE)
GET    /search?q=&genre=&page=&size=

POST   /uploads                   (multipart, rate-limited, auth)
GET    /uploads/{id}

GET    /playlists/{id}
POST   /playlists                 (auth)
PATCH  /playlists/{id}            (auth, optimistic lock)
POST   /playlists/{id}/copy       (auth)
POST   /playlists/{id}/tracks     (auth)

POST   /ratings                   (auth, target=TRACK|PLAYLIST)

GET    /changes?since=cursor      (auth, rate-limited)
```

Every write: JWT required, Bucket4j/Redis rate limit, idempotency key supported.

---

## Database Schema (Initial Flyway)

Tables: `users`, `tracks`, `subtitle_lines`, `playlists`, `playlist_tracks`, `ratings`, `uploads_idempotency`, `provider_manifests` (if registry endpoint added).

> All PKs and FKs use PostgreSQL `uuid` type. No auto-increment integers for entity IDs. `uploads_idempotency.idempotency_key` stays `text`.

Indexes:

- `tracks` — GIN on `tsvector`, btree on `(genre, updated_at)`
- `playlists` — btree on `(owner_id, updated_at)`
- `playlist_tracks` — btree on `(playlist_id, position)`
- `ratings` — unique `(user_id, target_type, target_id)`, btree on `(target_type, target_id)`
- Full sync support — btree on `updated_at` across sync-exposed tables

---

## Post-MVP (Backend)

| Addition                      | Value                   |
| ----------------------------- | ----------------------- |
| CDN for HLS                   | Scale streaming         |
| WebSocket for collab          | Real-time playlists     |
| Recommendations engine        | Retention               |
| Signed provider marketplace   | Trust                   |
| E2E-encrypted self-hosted lib | Privacy                 |
| Social graph (follow, feed)   | Network effects         |
