# TASK011 — Mobile M6: Subtitle Fetch + Sync Display

## Status
Completed

## Goal
Fetch subtitle cues from backend and render the active cue in PlayerScreen synced to ExoPlayer playback position.

## Deliverables

### Network
- `SubtitleLine` data class (`startMs`, `endMs`, `text`).
- `SubtitleApiService` — `GET /api/tracks/{id}/subtitles`.
- `SubtitleRepository` — fetch + cache cues list.

### ViewModel
- `PlayerViewModel`: load cues when track changes; expose `currentCue: StateFlow<String?>`.
- Active cue = first cue where `startMs <= positionMs < endMs`; null when none.

### UI
- `PlayerScreen`: show `currentCue` text below seek bar; fade in/out with `AnimatedVisibility`.
- Empty/null cue → hide, not blank space.

## Patterns
- Hilt `@Singleton` for SubtitleRepository (reuse OkHttp client already wired).
- No Room caching for subtitles — in-memory list per track sufficient.
- Reuse `PlayerController.syncPosition()` poll already running at 500ms for cue matching.

## Done when
- PlayerScreen shows correct subtitle cue during playback, updates smoothly as position advances.
- No cue shown when track has no subtitles.
