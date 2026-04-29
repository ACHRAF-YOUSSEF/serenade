# Code Review - 2026-04-26 Mobile Playback Keepalive

## Needs Fixing
- PlayerController used the singleton ExoPlayer directly but did not start the Media3 `SerenadePlayerService` before playback, so long playback could lose its foreground-service lifecycle anchor.
- PlayerModule did not configure network wake mode or media audio focus/noisy-device handling, increasing risk of stream interruption when the device idles.
- SerenadePlayerService released the app-scoped singleton ExoPlayer in `onDestroy()`, so service teardown could stop playback and leave PlayerController holding a released player.

## Selected Phase
Fix mobile playback lifecycle and keepalive behavior only. Do not write backend or frontend tests per user instruction.

## Fixed This Pass
- PlayerController now starts `SerenadePlayerService` before playQueue, resume, skip previous, and skip next.
- PlayerModule now configures Media3 ExoPlayer with `C.WAKE_MODE_NETWORK`, media audio attributes, audio focus, and noisy-device handling.
- SerenadePlayerService now releases only MediaSession in `onDestroy()`, not the app-scoped singleton ExoPlayer.

## Verification
- `./gradlew assembleDebug --no-daemon --console=plain` passed.
- `git diff --check` passed.

## Still Needs Work
- No device playback soak test was run from this environment.
- Persisted playback queue/history remains pending.
