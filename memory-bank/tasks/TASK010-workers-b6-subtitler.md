# TASK010 — Workers B6: Subtitler Worker

## Status
Completed

## Goal
Python worker: consume RabbitMQ message → download audio from MinIO → transcribe with faster-whisper → push WebVTT cues to backend /internal/.

## Deliverables

### `workers/subtitler/`
- `whisper_pipeline.py` — faster-whisper transcription → list of `(start_ms, end_ms, text)` segments.
- `main.py` — aio-pika consumer; on message: MinIO download → whisper_pipeline → httpx POST to `/internal/tracks/{id}/subtitles`.

### Shared reuse
- Use `workers/shared/minio_client.py` and `workers/shared/settings.py` (same pattern as transcoder).

## Security non-negotiables
- API key from `settings.INTERNAL_API_KEY` env var — never logged.
- No shell=True anywhere.
- faster-whisper model size configurable via env (`WHISPER_MODEL`, default `base`).

## RabbitMQ
- Queue: `subtitler` (new queue + binding in backend RabbitConfig).
- Message payload: same envelope as transcoder — `{ "trackId": "...", "objectKey": "..." }`.

## Done when
- Worker consumes message, transcribes audio, posts cues to backend.
- Backend stores subtitle_lines rows.
- `/api/tracks/{id}/subtitles` returns populated list.
