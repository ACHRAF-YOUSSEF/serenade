# TASK028 - Mobile Playback Keepalive Fix

**Status:** Completed

## Original Request
Fix the bug where the mobile media player stops after about 1 minute of playback.

## Thought Process
Review pointed at the mobile playback lifecycle, not the HLS generation path. HLS is generated as a full VOD playlist and served from `/hls/**`, while the Android player was using an app-scoped ExoPlayer without actively starting the MediaSessionService for long-running playback.

The likely failure mode is playback losing its long-running foreground-service anchor and/or network wake handling when the app idles or moves background. Another bug was that `SerenadePlayerService.onDestroy()` released the singleton ExoPlayer, so any service teardown could kill playback or break later playback attempts.

No backend/frontend tests are allowed by current user instruction, so verification is compile/static only.

## Implementation Plan
1. Start `SerenadePlayerService` before play/resume/skip.
2. Configure ExoPlayer for media audio focus, noisy-device handling, and network wake mode.
3. Stop the service from releasing the app-scoped singleton player.
4. Run Android assemble without tests.
5. Update memory-bank markdown.

## Progress Tracking

### Subtasks
| ID | Description | Status |
|----|-------------|--------|
| 1 | Review playback service/player wiring and HLS path | Complete |
| 2 | Apply mobile playback keepalive changes | Complete |
| 3 | Compile Android app without tests | Complete |
| 4 | Update memory docs | Complete |

## Progress Log

### 2026-04-26
- Reviewed PlayerController, PlayerModule, SerenadePlayerService, manifest service declaration, HLS backend controller, and worker HLS generation.
- Added foreground service start before Media3 play/resume/skip calls.
- Added `C.WAKE_MODE_NETWORK`, media audio attributes, audio focus, and noisy-device handling to the app-scoped ExoPlayer.
- Changed SerenadePlayerService to release only its MediaSession on destroy, leaving the singleton ExoPlayer owned by the app scope.
- Ran `./gradlew assembleDebug --no-daemon --console=plain`; passed.
- Ran `git diff --check`; clean.
