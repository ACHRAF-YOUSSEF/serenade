# Code Review — 2026-04-25 Sync + Worker Hardening

## Fixed This Pass
- Mobile outbox was still a placeholder: `SyncRepository.flushPendingOpsPlaceholder()` read pending rows but never pushed them. Playlist create/copy and playlist/track rating now write `PendingOpEntity` rows, and `SyncRepository` drains supported ops.
- Playlist UI called remote APIs directly, so offline playlist creation had no local-first path. `PlaylistRepository` now writes Room first, queues outbox work, then tries remote sync.
- Rating UI called remote APIs directly, so offline ratings were dropped. `RatingRepository` now writes local rating state, queues outbox work, and clears the op after remote success.
- Workers parsed RabbitMQ JSON manually and trusted message object paths. Both workers now validate with Pydantic and derive `raw/{trackId}` from validated UUID.
- Workers made raw `httpx` calls with `X-Api-Key` per callback. Calls now go through shared `SpringClient`.
- Worker API key setting used `internal_api_key`; workers now require `WORKER_API_KEY`, log only key presence, and never log the value.
- Android debug HTTP logging could expose `Authorization`; logging now redacts that header.
- Backend committed default JWT/internal API secrets in YAML. Defaults are removed; backend fails fast when `INTERNAL_API_KEY` is missing, and `.env-example` documents required keys.
- Memory task files TASK009-TASK011 had stale `in_progress` status despite completed implementation.

## Still Needs Work
- Mobile upload UI/status is next: SAF picker, multipart upload, progress, and track status polling.
- Upload pending ops are not implemented yet because there is no mobile upload repository/UI.
- Playlist add/remove/reorder UI still pending; outbox currently covers create/copy/rating only.
- Workers still lack FastAPI `/health`, `/metrics`, and protected `/admin/reprocess` surfaces.
- Full HLS package download remains pending for `.m3u8` manifests.
- Backend rate limiting/security headers/observability remain B9-B10 work.
