# TASK016 - Downloads and Offline Playback

**Status:** Completed  
**Added:** 2026-04-25  
**Updated:** 2026-04-25

## Original Request
Move to the next phase and update relevant markdown files such as `progress.md`.

## Thought Process
The next documented phase was mobile downloads/offline playback. The app already had `DownloadEntity`, `DownloadDao`, WorkManager dependencies, and local path fields on `TrackEntity`, but no download worker, repository, screen, or player source resolver.

## Implementation Plan
- Add download DAO one-shot/update/delete helpers.
- Preserve existing local download metadata during track sync.
- Add `DownloadRepository` with WorkManager enqueue/delete flow.
- Add Hilt `DownloadWorker` that saves a stream URL into app-private storage, updates Room progress/state, marks tracks downloaded, and posts completion notification when allowed.
- Add `DownloadScreen` and `DownloadViewModel`.
- Wire download buttons in track rows, Downloads route, and local-path-first playback source resolution.
- Verify Android build without tests.
- Update progress docs.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Inspect current download/player model | Complete | 2026-04-25 | Existing Room fields found |
| 2 | Add download data + worker flow | Complete | 2026-04-25 | Repository, worker, Hilt WorkManager config |
| 3 | Add UI and playback wiring | Complete | 2026-04-25 | Row actions, Downloads screen, local URI playback |
| 4 | Verify build | Complete | 2026-04-25 | `assembleDebug -x test` passed |
| 5 | Update docs | Complete | 2026-04-25 | Root progress and memory bank updated |

## Progress Log
### 2026-04-25
- Added `DownloadRepository`, Hilt `DownloadWorker`, WorkManager configuration in `SerenadeApplication`, and notification permission/channel handling.
- Added download row action in `TrackListScreen`, `DownloadScreen`, `DownloadViewModel`, and `ROUTE_DOWNLOADS`.
- `TrackSyncRepository` now preserves downloaded/localPath state when refreshing remote tracks.
- `AppNavigation` resolves playback to `localPath` first, then `streamUrl`.
- Verified `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`.
- Limitation: worker downloads a single stream URL to app-private storage. Full HLS package download is still pending if backend stream URLs point to `.m3u8` manifests.
