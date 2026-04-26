from __future__ import annotations

import re
import uuid
from contextvars import ContextVar, Token
from typing import Any, Mapping

REQUEST_ID_HEADER = "X-Request-Id"

_SAFE_REQUEST_ID = re.compile(r"[A-Za-z0-9._:-]{1,128}")
_request_id: ContextVar[str | None] = ContextVar("request_id", default=None)


def current_request_id() -> str | None:
    return _request_id.get()


def set_request_id(value: str | None) -> Token[str | None]:
    return _request_id.set(value)


def reset_request_id(token: Token[str | None]) -> None:
    _request_id.reset(token)


def request_id_from_headers(headers: Mapping[str, Any] | None) -> str:
    if headers:
        for key, value in headers.items():
            if key.lower() == REQUEST_ID_HEADER.lower():
                normalized = normalize_request_id(value)
                if normalized is not None:
                    return normalized
    return str(uuid.uuid4())


def normalize_request_id(value: Any) -> str | None:
    if value is None:
        return None
    if isinstance(value, bytes):
        try:
            value = value.decode("utf-8")
        except UnicodeDecodeError:
            return None
    normalized = str(value).strip()
    if _SAFE_REQUEST_ID.fullmatch(normalized):
        return normalized
    return None
