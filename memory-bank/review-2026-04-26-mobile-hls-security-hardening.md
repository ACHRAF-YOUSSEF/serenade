# Code Review — 2026-04-26 Mobile HLS + Security Hardening

## Needs Fixing
- `DownloadWorker` stored only the manifest when `streamUrl` pointed at `.m3u8`, so offline playback would fail once segment URLs expired or network disappeared.
- Android manifest enabled app-wide cleartext and network security config also allowed cleartext for the production domain.
- Backup/data extraction XML still used sample placeholders instead of explicit exclusion rules.
- Backend `contextLoads` still fails without datasource configuration, but user explicitly said not to write backend/frontend tests, so this pass did not modify tests.

## Selected Phase
Mobile offline playback/security hardening. Scope stays on production code/config only because tests are disallowed for this request.

## Fixed This Pass
- `DownloadWorker` detects `.m3u8` URLs, downloads manifest + segment files to app-private storage, rewrites manifest segment references to local files, stores the local manifest path, and cleans partial artifacts on failure.
- Removed app-wide `android:usesCleartextTraffic` and stale `tools:replace` from AndroidManifest.
- Removed production domain from cleartext network security config; only emulator/local/LAN dev hosts remain.
- Replaced sample backup/data extraction XML with explicit app-data exclusions.

## Still Needs Work
- Backend smoke test remains broken without an explicit datasource/test profile, but no test edits were made per instruction.
- Queue/playlist playback, upload status notifications, artwork upload, Redis-backed rate limiting, and worker retry/backoff remain next-phase candidates.
