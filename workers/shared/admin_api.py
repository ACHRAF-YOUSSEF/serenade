from __future__ import annotations

import logging
from dataclasses import dataclass, field
from typing import Any

import aio_pika
from fastapi import Depends, FastAPI, HTTPException, Request, status

from .models import TrackUploadedMessage
from .request_context import REQUEST_ID_HEADER, normalize_request_id
from .settings import settings

logger = logging.getLogger(__name__)


@dataclass
class WorkerState:
    name: str
    queue: str
    channel: aio_pika.abc.AbstractChannel | None = None
    processed: int = 0
    errors: int = 0


def create_admin_app(state: WorkerState) -> FastAPI:
    app = FastAPI(title=f"{state.name} admin", docs_url=None, redoc_url=None)

    def _require_api_key(request: Request) -> None:
        if request.headers.get("X-Api-Key", "") != settings.worker_api_key:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")

    @app.get("/health")
    async def health() -> dict[str, Any]:
        return {"status": "ok", "worker": state.name}

    @app.get("/metrics")
    async def metrics() -> dict[str, Any]:
        return {
            "worker": state.name,
            "processed": state.processed,
            "errors": state.errors,
        }

    @app.post(
        "/admin/reprocess/{track_id}",
        status_code=202,
        dependencies=[Depends(_require_api_key)],
    )
    async def reprocess(track_id: str, request: Request) -> dict[str, Any]:
        if state.channel is None:
            raise HTTPException(status_code=503, detail="RabbitMQ channel not ready")
        try:
            msg = TrackUploadedMessage(trackId=track_id)
            request_id = normalize_request_id(request.headers.get(REQUEST_ID_HEADER))
            await state.channel.default_exchange.publish(
                aio_pika.Message(
                    body=msg.model_dump_json().encode(),
                    headers={REQUEST_ID_HEADER: request_id} if request_id else None,
                ),
                routing_key=state.queue,
            )
        except Exception as exc:
            logger.exception("Reprocess publish failed for track %s", track_id)
            raise HTTPException(status_code=500, detail="Publish failed") from exc
        return {"queued": track_id}

    return app
