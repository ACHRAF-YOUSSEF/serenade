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
from shared.settings import settings
from shared.minio_client import get_minio
from shared.models import TrackUploadedMessage
from shared.spring_client import SpringClient
from whisper_pipeline import transcribe

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

QUEUE = "serenade.subtitler"


async def handle_message(
    message: aio_pika.IncomingMessage, *, state: WorkerState
) -> None:
    try:
        job = TrackUploadedMessage.model_validate_json(message.body)
    except ValidationError as exc:
        logger.warning("Rejecting malformed subtitle message: %s", exc.errors()[0]["type"])
        await message.reject(requeue=False)
        return

    async with message.process(requeue=False):
        track_id = job.track_id
        logger.info("Subtitler processing track %s", track_id)
        try:
            minio = get_minio()
            with tempfile.TemporaryDirectory() as tmp:
                audio_path = str(Path(tmp) / "audio")
                minio.fget_object(settings.minio_bucket, job.raw_object_key, audio_path)
                lines = transcribe(audio_path, track_id, model_size=settings.whisper_model)
            await _callback_subtitles(track_id, lines)
            state.processed += 1
        except Exception:
            logger.exception("Subtitler failed for track %s", track_id)
            state.errors += 1
            raise


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
