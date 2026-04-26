# TASK018 - Review Hardening and Outbox Phase

**Status:** Completed  
**Added:** 2026-04-25  
**Updated:** 2026-04-25

## Original Request
Review the codebase, write what needs to be improved or fixed, apply those changes, move to the next phase, and update relevant markdown files such as `progress.md`.

## Thought Process
The main blocker after sync pull was that mutation sync was only a placeholder: pending ops existed, but no mutation path emitted them and `SyncRepository` did not flush them. Review also found worker security drift from the project rules: manual JSON parsing, direct callback HTTP calls, and `internal_api_key` naming instead of `WORKER_API_KEY`.

Fix the highest-value cross-stack issues without expanding into the larger upload UI slice yet. Upload becomes the next phase after outbox foundation.

## Implementation Plan
- Document current review findings in a new memory-bank review note.
- Add mobile outbox payloads and flush support for playlist create/copy plus track/playlist ratings.
- Move playlist/rating ViewModels behind repositories that write local state and pending ops.
- Harden workers with Pydantic message validation and shared `SpringClient`.
- Remove committed backend secret defaults and redact Android debug auth logs.
- Verify backend, Android, and Python worker syntax.
- Update root progress and memory-bank progress/task files.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Review codebase gaps | Complete | 2026-04-25 | Outbox + worker security selected |
| 2 | Patch mobile outbox | Complete | 2026-04-25 | Pending op payloads, flush, playlist/rating repositories |
| 3 | Harden worker callbacks | Complete | 2026-04-25 | Pydantic validation + shared SpringClient |
| 4 | Clean backend/mobile security config | Complete | 2026-04-25 | No YAML secret defaults, auth header redaction |
| 5 | Verify builds | Complete | 2026-04-25 | Backend package, Android assemble, Python compile |
| 6 | Update docs | Complete | 2026-04-25 | Progress, active context, review note, task index |

## Progress Log
- 2026-04-25: Added `PlaylistRepository` and `RatingRepository`. Playlist create writes Room first, queues `CREATE_PLAYLIST`, then attempts remote creation. Copy/rating queue pending ops and clear them on remote success.
- 2026-04-25: Replaced `flushPendingOpsPlaceholder()` with real outbox draining for `CREATE_PLAYLIST`, `COPY_PLAYLIST`, `RATE_PLAYLIST`, and `RATE_TRACK`.
- 2026-04-25: Added worker `TrackUploadedMessage` Pydantic schema and `SpringClient`; workers now derive raw object key from validated `trackId` and use `WORKER_API_KEY`.
- 2026-04-25: Removed backend YAML defaults for JWT/internal API key; added `INTERNAL_API_KEY` startup validation and `.env-example` entries.
- 2026-04-25: Redacted Android `Authorization` header in debug OkHttp logs.
- 2026-04-25: Verified `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`, `sh mvnw -DskipTests clean package`, and `python3 -m compileall workers`.
