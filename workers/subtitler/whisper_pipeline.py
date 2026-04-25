import logging
import uuid

from faster_whisper import WhisperModel

logger = logging.getLogger(__name__)

_model: WhisperModel | None = None


def _get_model(model_size: str) -> WhisperModel:
    global _model
    if _model is None:
        _model = WhisperModel(model_size, compute_type="int8")
    return _model


def transcribe(audio_path: str, track_id: str, model_size: str = "base") -> list[dict]:
    """Transcribe audio; return list of {id, startMs, endMs, text} dicts."""
    model = _get_model(model_size)
    segments, _ = model.transcribe(audio_path, word_timestamps=False)
    results = []
    namespace = uuid.UUID("6ba7b810-9dad-11d1-80b4-00c04fd430c8")  # uuid.NAMESPACE_URL
    for i, seg in enumerate(segments):
        text = seg.text.strip()
        if not text:
            continue
        results.append({
            "id": str(uuid.uuid5(namespace, f"{track_id}-{i}")),
            "startMs": int(seg.start * 1000),
            "endMs": int(seg.end * 1000),
            "text": text,
        })
    logger.info("Transcribed %d segments from %s", len(results), audio_path)
    return results
