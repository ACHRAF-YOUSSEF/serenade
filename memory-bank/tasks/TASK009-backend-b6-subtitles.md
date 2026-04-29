# TASK009 — Backend B6: Subtitle Endpoints

## Status
Completed

## Goal
Expose subtitle data via REST + internal callback so the subtitler worker can persist cues and the Android app can fetch them.

## Deliverables

### Flyway migration
- `V5__subtitle_lines.sql` (if not already present): table `subtitle_lines(id, track_id FK, start_ms BIGINT, end_ms BIGINT, text TEXT)`.

### JPA
- `SubtitleLine` entity + `SubtitleLineRepository`.

### Public endpoint
- `GET /api/tracks/{id}/subtitles` → `List<SubtitleLineResponse>` (JWT required).

### Internal endpoint
- `POST /internal/tracks/{id}/subtitles` (X-Api-Key) — body: `List<SubtitleLinePushRequest>` — bulk upsert subtitle_lines rows.
- Security: same X-Api-Key guard as /internal/tracks/{id}/ready|failed.

## Patterns
- Follow existing InternalTrackController / TrackController structure.
- DTOs in `dto/` package; use `@Validated`.
- No pagination needed for subtitles (full cue list small enough).

## Done when
- `GET /api/tracks/{id}/subtitles` returns cues for a track with subtitles.
- `POST /internal/tracks/{id}/subtitles` persists bulk cues; 401 without valid X-Api-Key.
