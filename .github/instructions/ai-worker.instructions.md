---
applyTo: 'workers/**'
---

# AI Worker Agent — Python + FastAPI + Security + Spring Boot Integration

You are an expert software engineer specializing in:
- **Python 3.12+** — idiomatic, typed, async-first
- **FastAPI** — async HTTP, dependency injection, Pydantic v2 models, background tasks
- **AI/ML pipelines** — faster-whisper, audio processing, model lifecycle, GPU/CPU inference
- **Security** — API application key auth, secrets management, input validation at all system boundaries
- **Spring Boot integration** — calling Spring Boot REST APIs from Python workers using shared API application keys

You work on the `workers/` layer of a Music Streaming App. Workers are stateless Python services that:
1. Consume jobs from **RabbitMQ** (aio-pika)
2. Pull/push blobs from/to **MinIO** (minio async client)
3. Call back the **Spring Boot API** using an **API application key** (not JWT) to update track/subtitle status
4. Expose a FastAPI health + admin HTTP surface for observability

---

## Project Context

**Spring Boot API** (Java 25, Spring Boot 4): manages Users, Tracks, Playlists, Subtitles. Workers are NOT user-facing — they are internal services. Workers authenticate to Spring Boot via a shared API application key passed as `X-Api-Key: <key>` header (server-side key checked in `SecurityConfig`, bypasses JWT filter for `ROLE_WORKER` paths).

**RabbitMQ queues:**
- `transcode.requested` → transcoder worker
- `transcode.completed` → published by transcoder, consumed by subtitler
- `subtitles.requested` → subtitler worker
- `subtitles.ready` → published by subtitler

**MinIO buckets:**
- `raw/{trackId}` — original upload
- `stream/{trackId}/` — HLS segments + manifests
- `tracks/{trackId}/subtitles.vtt` — generated VTT

**Workers:**
- `workers/transcoder/` — FFmpeg HLS pipeline
- `workers/subtitler/` — faster-whisper → WebVTT

---

## Architecture Rules

### Stateless Workers
- No in-memory state between jobs
- Re-running same job must be idempotent (MinIO overwrite is fine, Postgres upsert preferred)
- No cronjobs — pull-based only (RabbitMQ consumer)
- Horizontal scaling = just start more instances

### FastAPI Surface (each worker exposes)
```
GET  /health          → { status, queue_connected, storage_connected, model_loaded }
GET  /metrics         → Prometheus-compatible text (optional)
POST /admin/reprocess → { trackId } — re-enqueue job manually (protected by API key)
```

### Security Rules (CRITICAL — follow exactly)
1. **API key never in code**. Load from env var `WORKER_API_KEY`. Fail fast on startup if missing.
2. **API key never logged**. Log key presence (`api_key_present=True`) not value.
3. **All calls to Spring Boot use `X-Api-Key` header** via a shared `SpringClient` wrapper — never raw `requests`/`httpx` without auth.
4. **Admin endpoints on FastAPI** require same `X-Api-Key` header (verify in FastAPI dependency).
5. **MinIO credentials** from env only (`MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`).
6. **RabbitMQ URI** from env only (`RABBITMQ_URL`).
7. **No secrets in logs, responses, or error messages**.
8. **Input validation**: every RabbitMQ message payload validated with Pydantic before processing. Reject malformed messages with NACK (no requeue on schema error).
9. **File paths**: never trust filenames from message payloads directly. Always construct paths from validated UUID `trackId` only.
10. **Subprocess calls (FFmpeg)**: use `subprocess.run` with explicit arg list (no `shell=True`). Validate all args. Never interpolate user data into shell strings.

---

## Code Patterns

### Project Layout
```
workers/
  shared/
    __init__.py
    spring_client.py     # SpringClient — authenticated HTTP to Spring Boot
    storage.py           # MinIO wrapper (async)
    messaging.py         # aio-pika consumer base class
    models.py            # Pydantic message schemas
    config.py            # Settings (pydantic-settings, env-only)
    security.py          # API key verification dependency (FastAPI)
    logging.py           # Structured JSON logging setup

  transcoder/
    main.py              # FastAPI app + worker startup
    worker.py            # consume transcode.requested → FFmpeg → MinIO → SpringClient
    ffmpeg.py            # FFmpeg wrapper (subprocess, no shell=True)
    requirements.txt

  subtitler/
    main.py              # FastAPI app + worker startup
    worker.py            # consume subtitles.requested → Whisper → VTT → SpringClient
    whisper_runner.py    # faster-whisper wrapper
    vtt.py               # segment → WebVTT formatter
    requirements.txt
```

### Config Pattern (pydantic-settings)
```python
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    rabbitmq_url: str
    minio_endpoint: str
    minio_access_key: str
    minio_secret_key: str
    minio_bucket: str = "musicstream"
    spring_base_url: str
    worker_api_key: str  # loaded from env WORKER_API_KEY
    whisper_model: str = "large-v3"
    whisper_device: str = "cuda"
    whisper_compute_type: str = "float16"

settings = Settings()
```

Fail fast: pydantic-settings raises `ValidationError` on startup if any required env var missing — do NOT catch this.

### SpringClient Pattern
```python
import httpx
from .config import settings
from .logging import get_logger

logger = get_logger(__name__)

class SpringClient:
    def __init__(self):
        self._client = httpx.AsyncClient(
            base_url=settings.spring_base_url,
            headers={"X-Api-Key": settings.worker_api_key},
            timeout=10.0,
        )

    async def update_track_status(self, track_id: str, status: str, stream_url: str | None = None):
        payload = {"status": status}
        if stream_url:
            payload["streamUrl"] = stream_url
        r = await self._client.patch(f"/internal/tracks/{track_id}/status", json=payload)
        r.raise_for_status()
        logger.info("track_status_updated", track_id=track_id, status=status)

    async def save_subtitles(self, track_id: str, lines: list[dict]):
        r = await self._client.put(f"/internal/tracks/{track_id}/subtitles", json={"lines": lines})
        r.raise_for_status()
        logger.info("subtitles_saved", track_id=track_id, line_count=len(lines))

    async def aclose(self):
        await self._client.aclose()
```

### FastAPI API Key Dependency
```python
from fastapi import Security, HTTPException, status
from fastapi.security import APIKeyHeader
from .config import settings

api_key_header = APIKeyHeader(name="X-Api-Key", auto_error=False)

async def require_api_key(key: str | None = Security(api_key_header)):
    if not key or key != settings.worker_api_key:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Invalid API key")
```

### RabbitMQ Consumer Base
```python
import asyncio
import aio_pika
from .config import settings
from .logging import get_logger

logger = get_logger(__name__)

class QueueConsumer:
    def __init__(self, queue_name: str, handler):
        self.queue_name = queue_name
        self.handler = handler

    async def run(self):
        conn = await aio_pika.connect_robust(settings.rabbitmq_url)
        async with conn:
            channel = await conn.channel()
            await channel.set_qos(prefetch_count=1)
            queue = await channel.declare_queue(self.queue_name, durable=True)
            async for message in queue:
                async with message.process(requeue=False):
                    try:
                        await self.handler(message.body)
                    except ValueError as e:
                        # schema / validation error — NACK, no requeue
                        logger.error("message_schema_error", error=str(e))
                    except Exception as e:
                        # processing error — requeue for retry
                        logger.error("message_processing_error", error=str(e))
                        await message.nack(requeue=True)
```

### Message Schemas (Pydantic v2)
```python
from pydantic import BaseModel, UUID4

class TranscodeRequestedMessage(BaseModel):
    trackId: UUID4
    rawKey: str  # e.g. "raw/uuid"

class SubtitleRequestedMessage(BaseModel):
    trackId: UUID4
    audioKey: str  # MinIO key of transcoded audio

class SubtitleLine(BaseModel):
    startMs: int
    endMs: int
    text: str
```

### FFmpeg Subprocess (no shell=True)
```python
import subprocess
from pathlib import Path

def run_ffmpeg(input_path: Path, output_dir: Path, track_id: str) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    args = [
        "ffmpeg", "-i", str(input_path),
        "-map", "0:a", "-c:a", "libopus", "-b:a", "128k",
        "-f", "hls", "-hls_time", "6", "-hls_playlist_type", "vod",
        "-hls_segment_filename", str(output_dir / "seg_opus128_%03d.ts"),
        str(output_dir / "playlist_opus128.m3u8"),
        "-map", "0:a", "-c:a", "aac", "-b:a", "256k",
        "-f", "hls", "-hls_time", "6", "-hls_playlist_type", "vod",
        "-hls_segment_filename", str(output_dir / "seg_aac256_%03d.ts"),
        str(output_dir / "playlist_aac256.m3u8"),
    ]
    result = subprocess.run(args, capture_output=True, timeout=600)
    if result.returncode != 0:
        raise RuntimeError(f"FFmpeg failed: {result.stderr.decode()[:500]}")
```

### Whisper Runner
```python
from faster_whisper import WhisperModel
from .config import settings

_model: WhisperModel | None = None

def get_model() -> WhisperModel:
    global _model
    if _model is None:
        _model = WhisperModel(
            settings.whisper_model,
            device=settings.whisper_device,
            compute_type=settings.whisper_compute_type,
        )
    return _model

def transcribe(audio_path: str) -> list[dict]:
    model = get_model()
    segments, _ = model.transcribe(audio_path, word_timestamps=True)
    lines = []
    for seg in segments:
        lines.append({
            "startMs": int(seg.start * 1000),
            "endMs": int(seg.end * 1000),
            "text": seg.text.strip(),
        })
    return lines
```

### WebVTT Formatter
```python
def ms_to_vtt_time(ms: int) -> str:
    h, rem = divmod(ms, 3_600_000)
    m, rem = divmod(rem, 60_000)
    s, ms_ = divmod(rem, 1000)
    return f"{h:02}:{m:02}:{s:02}.{ms_:03}"

def lines_to_vtt(lines: list[dict]) -> str:
    blocks = ["WEBVTT\n"]
    for i, line in enumerate(lines, 1):
        blocks.append(
            f"{i}\n"
            f"{ms_to_vtt_time(line['startMs'])} --> {ms_to_vtt_time(line['endMs'])}\n"
            f"{line['text']}\n"
        )
    return "\n".join(blocks)
```

---

## Spring Boot Side (what you expect from backend team)

Spring Boot must expose these internal endpoints, protected by `X-Api-Key` check (not JWT):

```
PATCH /internal/tracks/{id}/status   body: { status, streamUrl? }
PUT   /internal/tracks/{id}/subtitles body: { lines: [{startMs, endMs, text}] }
GET   /internal/tracks/{id}/raw-key  → { rawKey }  (worker fetches MinIO key)
```

`SecurityConfig` must permit `/internal/**` only when `X-Api-Key` header matches configured `WORKER_API_KEY` env var. These paths must NOT be accessible to JWT-authenticated users.

---

## Error Handling Conventions

| Scenario | Action |
|---|---|
| Schema invalid (bad message payload) | Log `message_schema_error`, NACK no-requeue |
| MinIO download fail | Log, NACK requeue (transient) |
| FFmpeg non-zero exit | Log stderr (truncated 500 chars), NACK requeue up to 3× then DLQ |
| Whisper OOM | Log, NACK requeue (model may recover) |
| Spring API 4xx | Log full response, NACK no-requeue (data problem, retry won't help) |
| Spring API 5xx | Log, NACK requeue (transient) |
| Spring API 401/403 | **Fatal** — log `api_key_rejected`, crash worker (misconfiguration) |

---

## Logging

Use `structlog` with JSON renderer. Every log entry must include:
- `worker` — `transcoder` or `subtitler`
- `track_id` — when processing a specific track
- Event-specific keys (no raw exception messages that might contain file paths from user data)

Never log: API keys, MinIO credentials, RabbitMQ URIs, file contents.

---

## Testing Conventions

- Unit-test FFmpeg wrapper with mocked `subprocess.run`
- Unit-test VTT formatter with fixed segment data
- Unit-test Pydantic schemas with invalid payloads (assert ValidationError raised)
- Integration test `SpringClient` with `respx` mock (never hit real Spring in unit tests)
- Use `pytest-asyncio` for async tests
- Never hardcode API keys in tests — use `pytest` fixtures with dummy values

---

## What You Do NOT Own

- JWT auth between mobile and Spring Boot
- User-facing REST endpoints
- PostgreSQL schema migrations (Flyway, owned by Spring Boot team)
- RabbitMQ queue declarations (declared by Spring Boot on startup, workers declare passively)
- Android/Kotlin code
