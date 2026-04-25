import asyncio
import logging
import sys

import aio_pika
from pydantic import ValidationError

sys.path.insert(0, str(__import__("pathlib").Path(__file__).parent.parent))

from shared.settings import settings
from shared.minio_client import get_minio
from shared.models import TrackUploadedMessage
from shared.spring_client import SpringClient
from hls_pipeline import run_pipeline

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

QUEUE = "serenade.transcoder"


async def handle_message(message: aio_pika.IncomingMessage) -> None:
    try:
        job = TrackUploadedMessage.model_validate_json(message.body)
    except ValidationError as exc:
        logger.warning("Rejecting malformed transcode message: %s", exc.errors()[0]["type"])
        await message.reject(requeue=False)
        return

    async with message.process(requeue=True):
        track_id = job.track_id
        logger.info("Processing track %s", track_id)

        try:
            minio = get_minio()
            stream_key, duration_ms = run_pipeline(
                track_id, job.raw_object_key, minio, settings.minio_bucket
            )
            await _callback_ready(track_id, stream_key, duration_ms)
        except Exception:
            logger.exception("Transcoding failed for track %s", track_id)
            await _callback_failed(track_id)
            raise


async def _callback_ready(track_id: str, stream_key: str, duration_ms: int) -> None:
    async with SpringClient() as client:
        await client.mark_ready(track_id, stream_key, duration_ms)
    logger.info("Marked track %s READY stream=%s duration=%dms", track_id, stream_key, duration_ms)


async def _callback_failed(track_id: str) -> None:
    async with SpringClient() as client:
        await client.mark_failed(track_id)
    logger.info("Marked track %s FAILED", track_id)


async def main() -> None:
    logger.info("Transcoder starting api_key_present=%s", bool(settings.worker_api_key))
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
