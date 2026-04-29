# Code Review — 2026-04-26 Mobile Connectivity

## Needs Fixing
- Mobile `BuildConfig.API_BASE_URL` was hardcoded to `http://192.168.0.218:8080/`.
- On Android emulator, that LAN IP can be unreachable or stale; search, download, and player all depend on the same backend URL, so they can fail together.
- Existing backend/mobile code already had public HLS proxy support and split public/authenticated OkHttp clients; the remaining project-specific failure point was the hardcoded base URL.

## Selected Phase
Mobile connectivity fix for search/download/player. Scope stayed in production config only; no backend/frontend tests were written.

## Fixed This Pass
- `app/app/build.gradle.kts` now reads `SERENADE_API_BASE_URL` from Gradle property first, then environment variable.
- Default API base URL is now `http://10.0.2.2:8080/`, the Android emulator host-loopback URL.
- Physical device builds can still target a LAN host by building with `SERENADE_API_BASE_URL=http://<host-lan-ip>:8080/`.

## Still Needs Work
- Add an in-app environment switch or debug settings screen if frequent emulator/device switching is needed.
- Runtime smoke check still requires backend stack running with uploaded READY tracks; only compile/static verification was run here.
