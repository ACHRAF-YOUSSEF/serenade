import httpx

from .settings import settings


class SpringClient:
    def __init__(self) -> None:
        self._client = httpx.AsyncClient(
            base_url=settings.backend_url,
            headers={"X-Api-Key": settings.worker_api_key},
            timeout=60.0,
        )

    async def mark_ready(self, track_id: str, stream_url: str, duration_ms: int) -> None:
        response = await self._client.post(
            f"/internal/tracks/{track_id}/ready",
            json={"streamUrl": stream_url, "durationMs": duration_ms},
        )
        response.raise_for_status()

    async def mark_failed(self, track_id: str) -> None:
        response = await self._client.post(f"/internal/tracks/{track_id}/failed")
        response.raise_for_status()

    async def push_subtitles(self, track_id: str, lines: list[dict]) -> None:
        response = await self._client.post(
            f"/internal/tracks/{track_id}/subtitles",
            json=lines,
        )
        response.raise_for_status()

    async def aclose(self) -> None:
        await self._client.aclose()

    async def __aenter__(self) -> "SpringClient":
        return self

    async def __aexit__(self, exc_type, exc, tb) -> None:
        await self.aclose()
