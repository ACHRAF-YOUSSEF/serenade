# TASK026 - Mobile Search Download Player Connectivity Fix

**Status:** Completed

## Original Request
Search, download, and media player do not work in the mobile app.

## Thought Process
These three features share one dependency: the mobile app must reach the backend base URL. Review found `BuildConfig.API_BASE_URL` hardcoded to a LAN address (`192.168.0.218`). That breaks emulator runs and any network where the host IP changes. The backend already exposes `/hls/**`, and current mobile code already uses separate public/authenticated OkHttp paths, so the safest fix is making the base URL configurable and defaulting to the Android emulator loopback.

No tests are allowed by current user instruction, so verification is compile/static only.

## Implementation Plan
1. Replace hardcoded mobile API base URL with Gradle property/env configuration.
2. Keep emulator default as `http://10.0.2.2:8080/`.
3. Verify Android assemble and backend compile without tests.
4. Update memory-bank documentation.

## Progress Tracking

| Subtask | Status |
| --- | --- |
| Review shared failure path for search/download/player | Done |
| Make API base URL configurable | Done |
| Run compile-only verification | Done |
| Update memory-bank markdown | Done |

## Progress Log

### 2026-04-26
- Reviewed SearchViewModel/SearchApiService, TrackList download flow, DownloadWorker, PlayerController, NetworkModule, PlayerModule, backend HLS controller/security, and Gradle build config.
- Changed `app/app/build.gradle.kts` so `SERENADE_API_BASE_URL` Gradle property/env overrides the API endpoint.
- Set default endpoint to `http://10.0.2.2:8080/` for emulator.
- Ran backend compile with `sh mvnw -q -DskipTests compile`; passed.
- Ran Android compile with `./gradlew assembleDebug --no-daemon --console=plain`; passed.
- Ran `git diff --check`; clean.
