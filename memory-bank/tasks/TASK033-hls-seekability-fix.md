# TASK033 - HLS Seekability Fix

**Status:** Completed  
**Added:** 2026-04-26  

## Original Request
User reported some uploaded HLS audio tracks around 3:24 and 3:37 play but cannot seek, while shorter tracks seek correctly. User suspected missing or unpicked content length.

## Thought Process
- Android displays duration from track metadata when Media3 timeline duration is unknown, so the slider can look seekable even when Media3 treats the HLS manifest as non-seekable.
- Backend already attempts to set `Content-Length` for HLS files from MinIO response headers.
- HLS VOD seekability depends more on playlist metadata than segment object length. Serving old manifests as explicit VOD playlists should help without requiring re-transcode.
- New worker output should be audio-only and explicitly VOD.

## Implementation Plan
- Normalize served `index.m3u8` manifests by injecting `#EXT-X-PLAYLIST-TYPE:VOD` and `#EXT-X-ENDLIST` when missing.
- Preserve exact `Content-Length` for normalized manifests.
- Update transcoder FFmpeg args to generate audio-only VOD HLS manifests.
- Run focused build/compile checks.

## Subtasks
| Item | Status |
| --- | --- |
| Inspect player, HLS proxy, MinIO, transcoder code | Done |
| Patch backend HLS manifest response | Done |
| Patch worker FFmpeg HLS args | Done |
| Verify builds/compile | Done |
| Update memory bank | Done |

## Progress Log
### 2026-04-26
- Read memory bank and relevant agent instructions.
- Found `HlsController` streams `.m3u8` directly and sets content length if MinIO exposes size.
- Found worker FFmpeg command lacks explicit `-hls_playlist_type vod` and audio-only mapping.
- Updated `HlsController` to normalize served manifests as VOD and return exact byte length for the normalized response.
- Corrected `HlsController.getHlsFile` return type to `ResponseEntity<Resource>` because the endpoint can return either `ByteArrayResource` for manifests or `InputStreamResource` for TS segments.
- Updated `workers/transcoder/hls_pipeline.py` to generate audio-only VOD HLS manifests.
- Investigated device logs: HLS segment HTTP responses were healthy, but old segments showed Media3 `PesReader` warnings plus AVC decoder startup, indicating pre-fix HLS may include video/artwork streams and needs reprocessing with the new worker args.
- Hardened `SerenadePlayerService` by calling `ensureForeground()` in `onCreate()` before Media3 provider/session setup to avoid `ForegroundServiceDidNotStartInTimeException`.
- Verified with `cd backend && ./mvnw -q -DskipTests compile`, `python3 -m py_compile workers/transcoder/hls_pipeline.py`, and `cd app && ./gradlew assembleDebug --no-daemon --console=plain`.
