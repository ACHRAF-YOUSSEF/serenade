# TASK032 - Stable Artwork Display Fix

## Status
Completed

## Original Request
Fix artwork display so tracks that have artwork show it in the list, full player, and mini player instead of the music-note placeholder.

## Thought Process
The Compose screens already use Coil `AsyncImage` when `artworkUrl` is nonblank. The likely failure was the URL itself: backend stored MinIO presigned artwork URLs in `Track.artworkUrl`, which can expire and can point at a MinIO host/port the Android device cannot reach. Playback uses backend-host-normalized URLs; artwork needed the same stable-device-reachable path.

## Implementation Plan
1. Add a backend artwork proxy endpoint keyed by track id.
2. Keep object-key extraction constrained to `artwork/{uuid}.{jpg|jpeg|png|webp}`.
3. Normalize Android artwork URLs to the backend artwork endpoint whenever the track has artwork.
4. Compile backend and Android app.

## Progress Tracking

### Subtasks
| ID | Description | Status |
|----|-------------|--------|
| 1 | Inspect artwork backend/mobile flow | Complete |
| 2 | Add backend stable artwork endpoint | Complete |
| 3 | Normalize mobile artwork URL usage | Complete |
| 4 | Compile backend and Android app | Complete |
| 5 | Update memory docs | Complete |

## Progress Log

### 2026-04-26
- Added `TrackArtworkController` with `GET /artwork/{trackId}`.
- Endpoint loads track by UUID, derives a safe MinIO `artwork/{uuid}.{ext}` key from stored artwork value, streams object with image media type, and returns 404 for missing/invalid keys.
- Allowed `/artwork/**` through Spring Security.
- Added Android `stableArtworkUrl(trackId, artworkUrl)` helper.
- Updated `TrackSyncRepository` and search `TrackResponse.toEntity()` conversion to use stable backend artwork URLs instead of stored MinIO URLs.
- Ran `cd backend && ./mvnw -q -DskipTests compile`; passed.
- Ran `cd app && ./gradlew assembleDebug --no-daemon --console=plain`; passed.
