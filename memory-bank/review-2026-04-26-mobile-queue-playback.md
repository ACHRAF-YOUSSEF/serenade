# Code Review — 2026-04-26 Mobile Queue Playback

## Needs Fixing
- `PlayerController.play()` replaced the whole Media3 playlist with one item, so next/previous controls could never work.
- Playlist detail and search result taps discarded surrounding track context, preventing playlist/result-set playback.
- `AppNavigation` stored one `nowPlayingTrack`; title/artist would not follow automatic Media3 item transitions.
- `PlayerController` logged stream URLs, which can expose presigned URLs in device logs.
- Player code always set HLS MIME type, even when playing local file/content URLs or non-HLS audio.

## Selected Phase
Mobile queue/playlist playback. Scope stayed in production Android code only because the user said not to write backend/frontend tests.

## Fixed This Pass
- Added `PlaybackItem` queue input to `PlayerController`, Media3 `setMediaItems()`, next/previous actions, and queue metadata in `PlaybackState`.
- PlayerScreen now enables skip buttons from state and shows queue position.
- AppNavigation keeps playback queue metadata and derives current title/artist from `currentTrackId`.
- SearchScreen and PlaylistDetailScreen now pass the visible queue along with the selected track, so result/playlist playback can continue through next/previous.
- Removed stream URL logging and only sets HLS MIME type for `.m3u8` playback URLs.

## Still Needs Work
- Queue state is in-memory only; persist queue/history if the app needs restore after process death.
- Home feed and downloads still enqueue only the selected track; broader feed queues can be added when pagination/ordering behavior is finalized.
- Upload status notifications, artwork upload, Redis-backed rate limiting, and worker retry/backoff remain next-phase candidates.
