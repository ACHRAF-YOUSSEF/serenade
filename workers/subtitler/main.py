import asyncio
import json
import logging
import sys
import tempfile
from pathlib import Path

import aio_pika
import httpx

sys.path.insert(0, str(Path(__file__).parent.parent))

from shared.settings import settings
from shared.minio_client import get_minio
from whisper_pipeline import transcribe

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

QUEUE = "serenade.subtitler"


async def handle_message(message: aio_pika.IncomingMessage) -> None:
    async with message.process(requeue=True):
        body = json.loads(message.body)
        track_id: str = body["trackId"]
        raw_key: str = body["rawKey"]
        logger.info("Subtitler processing track %s raw=%s", track_id, raw_key)
        try:
            minio = get_minio()
            with tempfile.TemporaryDirectory() as tmp:
                audio_path = str(Path(tmp) / "audio")
                minio.fget_object(settings.minio_bucket, raw_key, audio_path)
                lines = transcribe(audio_path, track_id, model_size=settings.whisper_model)
            await _callback_subtitles(track_id, lines)
        except Exception:
            logger.exception("Subtitler failed for track %s", track_id)
            raise


async def _callback_subtitles(track_id: str, lines: list[dict]) -> None:
    url = f"{settings.backend_url}/internal/tracks/{track_id}/subtitles"
    # API key from env only, never logged
    headers = {"X-Api-Key": settings.internal_api_key}
    async with httpx.AsyncClient() as client:
        resp = await client.post(url, json=lines, headers=headers, timeout=60)
        resp.raise_for_status()
    logger.info("Pushed %d subtitle lines for track %s", len(lines), track_id)


async def main() -> None:
    connection = await aio_pika.connect_robust(settings.rabbitmq_url)
    async with connection:
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=1)
        queue = await channel.get_queue(QUEUE)
        logger.info("Subtitler worker listening on %s", QUEUE)
        await queue.consume(handle_message)
        await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())
