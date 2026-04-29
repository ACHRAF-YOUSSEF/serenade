# TASK034 - Persisted Playback Queue and History

## Status
Completed on 2026-04-26

## Original Request
Move to the next phase and update relevant markdown files including `progress.md`.

## Thought Process
- Memory bank showed post-TASK033 with next build slice including FCM, persisted playback queue/history, search thumbnails, and OpenTelemetry.
- Chose persisted playback queue/history as the next local phase because it requires no external Firebase or tracing backend setup.
- Queue persistence must support tracks from search/API responses before they are cached in Room, so playback tables intentionally avoid foreign keys to `tracks`.

## Implementation Plan
1. Add Room entities/DAO/repository for playback queue and recently played history.
2. Wire entities into `AppDatabase`, DI, and a release-safe 1->2 migration.
3. Save queues when playback starts, restore the last queue on app startup, and persist current item/position.
4. Let mini/full player fall back to Media3 metadata after restored queues.
5. Verify Android build and update progress docs.

## Subtasks
| Item | Status |
| --- | --- |
| Playback Room tables | Done |
| Queue save/restore | Done |
| Recently played history | Done |
| UI metadata fallback | Done |
| Android build verification | Done |
| Memory/progress docs | Done |

## Progress Log
- 2026-04-26: Added `playback_queue` and `playback_history` Room tables plus DAO/repository.
- 2026-04-26: Added AppDatabase version 2 and migration for playback persistence.
- 2026-04-26: Updated PlayerController to restore saved queue without autoplay, persist queue/current position, and record history when tracks start playing.
- 2026-04-26: Updated AppNavigation so restored Media3 metadata feeds mini player and full player.
- 2026-04-26: Verified `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`.
- 2026-04-26: Fixed launch crash in 1->2 migration by changing playback timestamp columns from `TEXT` to `INTEGER`, matching `AppConverters.fromInstant()` and Room schema validation. Migration now drops the new playback tables before recreating them so devices that hit the failed migration can recover.
