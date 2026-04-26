# TASK017 - Review Fixes and Sync Phase

**Status:** Completed  
**Added:** 2026-04-25  
**Updated:** 2026-04-25

## Original Request
Review the codebase, write what needs to be improved or fixed, apply those changes, move to the next phase, and update relevant markdown files such as `progress.md`.

## Thought Process
Review found the next critical product gap was sync: the app had `PendingOpEntity` but no backend `/changes` endpoint or mobile sync worker. The previous download phase also documented a known HLS package limitation. Rather than expanding UI polish, build the sync foundation required by the plan.

Key review findings:
- No backend changes feed existed, so mobile could not pull server-side updates by cursor.
- No mobile `SyncWorker` or periodic pull existed.
- Pending-op flush is not wired because mutation repositories still call remote APIs directly and do not create outbox entries.
- TASK004 memory file was stale: its index said completed, but the task file still said in progress.
- DownloadWorker stores a single URL; full HLS package download remains a known follow-up.

## Implementation Plan
- Add backend `GET /api/changes?since=&limit=` with tracks/playlists/ratings and `nextCursor`.
- Add repository methods for updated-since queries.
- Extend rating response with `updatedAt`.
- Add Android changes API DTO/service.
- Add `SyncRepository` and Hilt `SyncWorker`.
- Schedule periodic sync and run pull during track-list refresh.
- Verify backend and Android builds.
- Update root and memory-bank progress docs.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Review codebase gaps | Complete | 2026-04-25 | Sync pull chosen as next fix/phase |
| 2 | Add backend changes endpoint | Complete | 2026-04-25 | Tracks/playlists/ratings plus cursor |
| 3 | Add mobile sync pull worker | Complete | 2026-04-25 | ChangesApiService, SyncRepository, SyncWorker |
| 4 | Verify builds | Complete | 2026-04-25 | Backend package + Android assemble passed |
| 5 | Update docs | Complete | 2026-04-25 | Progress/memory/task docs updated |

## Progress Log
### 2026-04-25
- Added backend `ChangesController`, `ChangesService`, and `ChangesResponse`.
- Added updated-since repository methods for tracks, playlists, and user ratings.
- Added `updatedAt` to backend and Android rating responses and mobile track/playlist DTOs.
- Added Android `ChangesApiService`, `ChangesResponse`, `SyncRepository`, and Hilt `SyncWorker`.
- `TrackListViewModel` now schedules periodic sync and pulls changes during refresh.
- Verified `sh mvnw -DskipTests package` and `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`.
- Pending-op flushing remains a placeholder until repositories write mutation outbox rows.
