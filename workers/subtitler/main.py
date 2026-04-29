import asyncio
import logging
import sys
import tempfile
from functools import partial
from pathlib import Path

import aio_pika
import uvicorn
from pydantic import ValidationError

sys.path.insert(0, str(Path(__file__).parent.parent))

from shared.admin_api import WorkerState, create_admin_app
from shared.logging import configure_json_logging
from shared.request_context import request_id_from_headers, reset_request_id, set_request_id
from shared.settings import settings
from shared.minio_client import get_minio
from shared.models import TrackUploadedMessage
from shared.spring_client import SpringClient
from whisper_pipeline import transcribe

configure_json_logging()
logger = logging.getLogger(__name__)

QUEUE = "serenade.subtitler"


MAX_RETRIES = 3
RETRY_DELAYS_S = [5, 30, 120]


async def handle_message(
    message: aio_pika.IncomingMessage, *, state: WorkerState
) -> None:
    token = set_request_id(request_id_from_headers(message.headers))
    try:
        try:
            job = TrackUploadedMessage.model_validate_json(message.body)
        except ValidationError as exc:
            logger.warning("Rejecting malformed subtitle message: %s", exc.errors()[0]["type"])
            await message.reject(requeue=False)
            return

        retry_count = int(message.headers.get("x-retry-count", 0))
        track_id = job.track_id
        logger.info(
            "Subtitler processing track %s (attempt %d/%d)", track_id, retry_count + 1, MAX_RETRIES + 1
        )

        try:
            minio = get_minio()
            with tempfile.TemporaryDirectory() as tmp:
                audio_path = str(Path(tmp) / "audio")
                minio.fget_object(settings.minio_bucket, job.raw_object_key, audio_path)
                lines = transcribe(audio_path, track_id, model_size=settings.whisper_model)
            await _callback_subtitles(track_id, lines)
            state.processed += 1
            await message.ack()
        except Exception:
            state.errors += 1
            logger.exception("Subtitler failed for track %s", track_id)
            if retry_count < MAX_RETRIES:
                delay = RETRY_DELAYS_S[retry_count]
                logger.warning(
                    "Retrying subtitler for track %s in %ds (attempt %d/%d)",
                    track_id, delay, retry_count + 1, MAX_RETRIES + 1,
                )
                await asyncio.sleep(delay)
                await state.channel.default_exchange.publish(
                    aio_pika.Message(
                        body=message.body,
                        headers={**dict(message.headers), "x-retry-count": retry_count + 1},
                        delivery_mode=aio_pika.DeliveryMode.PERSISTENT,
                    ),
                    routing_key=QUEUE,
                )
                await message.ack()
            else:
                logger.error(
                    "Subtitler failed for track %s after %d attempts, dead-lettering",
                    track_id, MAX_RETRIES + 1,
                )
                await message.reject(requeue=False)
    finally:
        reset_request_id(token)


async def _callback_subtitles(track_id: str, lines: list[dict]) -> None:
    async with SpringClient() as client:
        await client.push_subtitles(track_id, lines)
    logger.info("Pushed %d subtitle lines for track %s", len(lines), track_id)


async def main() -> None:
    logger.info("Subtitler starting api_key_present=%s", bool(settings.worker_api_key))
    state = WorkerState(name="subtitler", queue=QUEUE)
    admin_app = create_admin_app(state)

    connection = await aio_pika.connect_robust(settings.rabbitmq_url)
    async with connection:
        channel = await connection.channel()
        state.channel = channel
        await channel.set_qos(prefetch_count=1)
        queue = await channel.get_queue(QUEUE)
        logger.info("Subtitler worker listening on %s", QUEUE)
        await queue.consume(partial(handle_message, state=state))

        config = uvicorn.Config(
            admin_app,
            host=settings.admin_host,
            port=settings.subtitler_admin_port,
            log_level="warning",
        )
        server = uvicorn.Server(config)
        logger.info("Admin API on port %d", settings.subtitler_admin_port)
        await server.serve()


if __name__ == "__main__":
    asyncio.run(main())
