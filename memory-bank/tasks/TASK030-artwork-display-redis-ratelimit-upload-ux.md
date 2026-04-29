# TASK030 — Artwork Display + Redis Rate Limiting + Upload Outbox UX

**Status:** Completed 2026-04-26

## Changes

### Android

**PlayerScreen.kt**
- Added `artworkUrl: String?` param.
- `Surface` artwork container now layers `AsyncImage` (Coil) over `MusicNote` icon fallback.
- `ContentScale.Crop` fills the 280dp rounded surface; icon remains visible if image absent or loading fails.

**MiniPlayerBar.kt**
- Added `artworkUrl: String?` param.
- 40dp `Surface` thumbnail inserted before title/artist column; same icon+AsyncImage stack pattern.

**AppNavigation.kt**
- `MiniPlayerBar` and `PlayerScreen` calls now pass `artworkUrl = nowPlayingTrack?.artworkUrl`.

**TrackListScreen.kt**
- `TrackRow` `leadingContent` replaced with 48dp `Surface` using `AsyncImage` + `MusicNote` fallback.

**UploadScreen.kt**
- `ArtworkPickerCard` renders 40dp `AsyncImage` thumbnail immediately after URI selection; falls back to `Image` icon when no artwork chosen.

**SyncRepository.kt**
- `applyPendingOp` UPLOAD_TRACK branch calls `notifyUploadFileMissing(title)` before returning when local file is absent.
- `notifyUploadFileMissing` creates channel `uploads`, checks `POST_NOTIFICATIONS` permission, posts notification: "Upload failed — file no longer available".

### Backend

**RateLimitFilter.java**
- Removed `ConcurrentHashMap<String, Bucket>` as primary store.
- Injects `ReactiveStringRedisTemplate`; executes Lua `INCR + PEXPIRE` script with a windowed key (`rl:{ip}:{policy}:{windowIndex}`).
- Window expiry set to `2 × policy.refillPeriod()` to handle key overlap at window boundaries.
- Falls back to local bucket4j `Bucket` (kept as `localBuckets` map) on any Redis exception; logs warning.
- `X-Rate-Limit-Remaining` header set to `max(0, capacity - count)` on allowed responses.
- `Retry-After` computed as seconds to next window boundary.

## Files Modified
- `app/.../player/presentation/PlayerScreen.kt`
- `app/.../player/presentation/MiniPlayerBar.kt`
- `app/.../core/navigation/AppNavigation.kt`
- `app/.../track/presentation/TrackListScreen.kt`
- `app/.../upload/presentation/UploadScreen.kt`
- `app/.../sync/data/SyncRepository.kt`
- `backend/.../config/RateLimitFilter.java`
- `memory-bank/activeContext.md`
- `memory-bank/progress.md`
- `memory-bank/tasks/_index.md`
