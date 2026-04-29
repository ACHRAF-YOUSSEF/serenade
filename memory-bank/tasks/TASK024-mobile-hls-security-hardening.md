# TASK024 - Mobile HLS Offline + Android Security Hardening

**Status:** Completed

## Original Request
Review the codebase, write what needs to be improved or fixed, apply those changes, move to the next phase, and update relevant markdown files such as `progress.md`. Follow-up instruction: do not write any backend or frontend tests.

## Thought Process
Review after TASK023 found the highest-value non-test fixes were in Android offline playback and mobile security configuration:
- `DownloadWorker` treated every stream URL as a single file. Backend streams are HLS manifests, so offline downloads only saved `index.m3u8` and not its segments.
- Manifest-level `android:usesCleartextTraffic="true"` widened cleartext beyond the local development hosts already listed in `network_security_config.xml`.
- Production domain was listed in a cleartext domain-config.
- Backup/data extraction XML still contained sample placeholders instead of explicit app-data exclusions.

Backend `contextLoads` also fails without a datasource, but tests are explicitly out of scope for this request.

## Implementation Plan
1. Tighten Android manifest and XML security config without changing runtime feature behavior.
2. Extend DownloadWorker to package HLS manifests and segment files for offline playback.
3. Run compile-only verification, not tests.
4. Update memory-bank review, progress, active context, and task index.

## Progress Tracking

| Subtask | Status |
| --- | --- |
| Review current codebase and known issues | Done |
| Remove broad mobile cleartext and sample backup rules | Done |
| Add HLS package download support in DownloadWorker | Done |
| Run compile-only Android verification | Done |
| Update memory-bank markdown | Done |

## Progress Log

### 2026-04-26
- Reviewed current memory bank, known issues, Android download worker, manifest/network security config, and backup XML.
- Removed app-wide cleartext and stale manifest tools namespace.
- Limited cleartext network config to emulator/local/LAN dev hosts and removed production cleartext domain.
- Replaced sample backup/data extraction XML with explicit root exclusions.
- Updated `DownloadWorker` to download `.m3u8` manifests plus segment files, rewrite segment references locally, store local manifest paths, and clean partial downloads on failure.
- Ran `./gradlew assembleDebug --no-daemon --console=plain`; build passed. No tests were written or run after the user clarified the constraint.
