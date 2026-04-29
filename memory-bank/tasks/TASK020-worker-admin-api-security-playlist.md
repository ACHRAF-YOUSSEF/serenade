# TASK020 - Worker Admin API + Security Headers + Playlist Track Management

**Status:** Completed  
**Added:** 2026-04-25  
**Updated:** 2026-04-25

## Original Request
Move to the next phase after mobile upload, continue fixing reviewed gaps, and update progress docs.

## Thought Process
After upload/status, the next useful cross-stack slice was worker observability/admin endpoints, basic backend security headers, and playlist add/remove outbox support. A later runtime check found both workers shared one `ADMIN_PORT`, so local subtitler + transcoder startup conflicted on port `8001`; the fix is worker-specific admin ports.

## Implementation Plan
- Add shared worker FastAPI admin app with health, metrics, and protected reprocess endpoint.
- Run each worker's admin API alongside its RabbitMQ consumer.
- Add backend security headers.
- Add mobile playlist add/remove UI and outbox support.
- Split worker admin ports so transcoder and subtitler can run together locally.
- Update docs and verify builds/syntax.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Worker admin API | Complete | 2026-04-25 | `/health`, `/metrics`, `/admin/reprocess/{track_id}` |
| 2 | Backend security headers | Complete | 2026-04-25 | Basic response hardening |
| 3 | Playlist add/remove outbox | Complete | 2026-04-25 | Add/remove operations drain through SyncRepository |
| 4 | Fix admin port conflict | Complete | 2026-04-25 | Transcoder `8000`, subtitler `8001` |
| 5 | Verify | Complete | 2026-04-25 | Python compile and diff check |

## Progress Log
- 2026-04-25: Added worker admin API, backend security headers, and playlist add/remove outbox according to current progress docs.
- 2026-04-25: Fixed local two-worker startup by replacing shared `ADMIN_PORT` with `TRANSCODER_ADMIN_PORT` and `SUBTITLER_ADMIN_PORT`; updated worker `.env-example` and requirements. Worker settings ignore extra dotenv keys so old local `ADMIN_PORT` entries do not break startup.
