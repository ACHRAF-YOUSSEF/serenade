import asyncio
import json
import logging
import sys

import aio_pika
import httpx

sys.path.insert(0, str(__import__("pathlib").Path(__file__).parent.parent))

from shared.settings import settings
from shared.minio_client import get_minio
from hls_pipeline import run_pipeline

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

QUEUE = "serenade.transcoder"


async def handle_message(message: aio_pika.IncomingMessage) -> None:
    async with message.process(requeue=True):
        body = json.loads(message.body)
        track_id: str = body["trackId"]
        raw_key: str = body["rawKey"]
        logger.info("Processing track %s raw=%s", track_id, raw_key)

        try:
            minio = get_minio()
            stream_key, duration_ms = run_pipeline(
                track_id, raw_key, minio, settings.minio_bucket
            )
            await _callback_ready(track_id, stream_key, duration_ms)
        except Exception:
            logger.exception("Transcoding failed for track %s", track_id)
            await _callback_failed(track_id)
            raise


async def _callback_ready(track_id: str, stream_key: str, duration_ms: int) -> None:
    url = f"{settings.backend_url}/internal/tracks/{track_id}/ready"
    # API key from env only, never logged
    headers = {"X-Api-Key": settings.internal_api_key}
    async with httpx.AsyncClient() as client:
        resp = await client.post(
            url,
            json={"streamUrl": stream_key, "durationMs": duration_ms},
            headers=headers,
            timeout=10,
        )
        resp.raise_for_status()
    logger.info("Marked track %s READY stream=%s duration=%dms", track_id, stream_key, duration_ms)


async def _callback_failed(track_id: str) -> None:
    url = f"{settings.backend_url}/internal/tracks/{track_id}/failed"
    headers = {"X-Api-Key": settings.internal_api_key}
    async with httpx.AsyncClient() as client:
        resp = await client.post(url, headers=headers, timeout=10)
        resp.raise_for_status()
    logger.info("Marked track %s FAILED", track_id)


async def main() -> None:
    connection = await aio_pika.connect_robust(settings.rabbitmq_url)
    async with connection:
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=1)
        queue = await channel.get_queue(QUEUE)
        logger.info("Transcoder worker listening on %s", QUEUE)
        await queue.consume(handle_message)
        await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())
