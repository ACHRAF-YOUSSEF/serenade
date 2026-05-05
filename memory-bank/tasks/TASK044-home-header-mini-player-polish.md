# TASK044 - Home Header and Mini Player Polish

Status: Completed

## Original Request
Use the user's display name for the home header avatar instead of hardcoded `J`, fall back to `U`, lower the mini-player progress bar a little, and make the Good evening header card shorter.

## Thought Process
The Listen header avatar was hardcoded. Current user data already exists in Room, so the track list view model can expose the saved username. The header height and progress bar placement were fixed dp values in existing Compose UI.

## Implementation Plan
- Read current user from `UserDao` in `TrackListViewModel`.
- Pass display name into `HomeHeader`.
- Derive avatar initial from display name, fallback to `U`.
- Reduce home header height.
- Move mini-player progress line closer to the lower card edge.
- Verify Android compile and diff check.

## Subtasks
| Status | Item |
| --- | --- |
| Completed | User-backed home avatar initial |
| Completed | Shorter Good evening header |
| Completed | Lower mini-player progress bar |
| Completed | Verify Android compile |

## Progress Log
- 2026-04-29: `TrackListViewModel` now exposes `displayName` from `UserDao.getCurrentUser()`.
- 2026-04-29: `HomeHeader` derives avatar text from display name first character, with `U` fallback, replacing hardcoded `J`.
- 2026-04-29: Home header height changed from `180.dp` to `150.dp`; right radial gradient center made finite.
- 2026-04-29: Mini-player row padding moved inside the card and progress line positioned with `bottom = 4.dp` so it sits lower. Verified `./gradlew :app:compileDebugKotlin`.
