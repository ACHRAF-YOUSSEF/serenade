from typing import Self

from pydantic import BaseModel, UUID4, model_validator


class TrackUploadedMessage(BaseModel):
    trackId: UUID4
    rawKey: str | None = None

    @model_validator(mode="after")
    def validate_raw_key(self) -> Self:
        if self.rawKey is not None and self.rawKey != self.raw_object_key:
            raise ValueError("rawKey must match raw/{trackId}")
        return self

    @property
    def track_id(self) -> str:
        return str(self.trackId)

    @property
    def raw_object_key(self) -> str:
        return f"raw/{self.track_id}"
