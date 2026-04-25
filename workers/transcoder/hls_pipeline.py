import json
import logging
import subprocess
import tempfile
from pathlib import Path

logger = logging.getLogger(__name__)


def probe_duration_ms(input_path: str) -> int:
    """Return track duration in milliseconds via ffprobe."""
    result = subprocess.run(
        [
            "ffprobe", "-v", "quiet",
            "-print_format", "json",
            "-show_format",
            input_path,
        ],
        capture_output=True,
        text=True,
        shell=False,  # security non-negotiable: explicit arg list, no shell
        check=True,
    )
    data = json.loads(result.stdout)
    duration_sec = float(data["format"].get("duration", 0))
    return int(duration_sec * 1000)


def transcode_to_hls(input_path: str, output_dir: str) -> str:
    """
    Transcode input audio to HLS.
    Returns the path to the generated .m3u8 playlist.
    """
    playlist = str(Path(output_dir) / "index.m3u8")
    segment_pattern = str(Path(output_dir) / "seg%03d.ts")

    subprocess.run(
        [
            "ffmpeg", "-y",
            "-i", input_path,
            "-c:a", "aac",
            "-b:a", "128k",
            "-ac", "2",
            "-hls_time", "6",
            "-hls_list_size", "0",
            "-hls_segment_filename", segment_pattern,
            "-f", "hls",
            playlist,
        ],
        capture_output=True,
        shell=False,  # security non-negotiable: explicit arg list, no shell
        check=True,
    )
    return playlist


def run_pipeline(track_id: str, raw_key: str, minio_client, bucket: str) -> tuple[str, int]:
    """
    Full pipeline: download raw → transcode → upload HLS → return (stream_url, duration_ms).
    """
    with tempfile.TemporaryDirectory() as tmp:
        raw_path = str(Path(tmp) / "raw_input")
        minio_client.fget_object(bucket, raw_key, raw_path)
        logger.info("Downloaded raw object for track %s", track_id)

        duration_ms = probe_duration_ms(raw_path)

        hls_dir = str(Path(tmp) / "hls")
        Path(hls_dir).mkdir()
        playlist_path = transcode_to_hls(raw_path, hls_dir)
        logger.info("HLS playlist generated for track %s", track_id)

        hls_prefix = f"hls/{track_id}"
        for file in Path(hls_dir).iterdir():
            object_key = f"{hls_prefix}/{file.name}"
            content_type = "application/vnd.apple.mpegurl" if file.suffix == ".m3u8" else "video/mp2t"
            minio_client.fput_object(bucket, object_key, str(file), content_type=content_type)
            logger.info("Uploaded %s", object_key)

    stream_key = f"{hls_prefix}/index.m3u8"
    return stream_key, duration_ms
