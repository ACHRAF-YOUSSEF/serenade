import logging

from .request_context import current_request_id


class _DevFormatter(logging.Formatter):
    _LEVEL_COLORS = {
        "DEBUG": "\033[36m",
        "INFO": "\033[32m",
        "WARNING": "\033[33m",
        "ERROR": "\033[31m",
        "CRITICAL": "\033[35m",
    }
    _RESET = "\033[0m"

    def format(self, record: logging.LogRecord) -> str:
        color = self._LEVEL_COLORS.get(record.levelname, "")
        ts = self.formatTime(record, "%H:%M:%S.%f")[:-3]
        worker = getattr(record, "worker", None)
        rid = current_request_id()
        ctx = ""
        if worker:
            ctx += f" [{worker}]"
        if rid:
            ctx += f" [{rid}]"
        base = f"{ts} {color}{record.levelname:>8}{self._RESET}{ctx} {record.name}: {record.getMessage()}"
        if record.exc_info:
            base += "\n" + self.formatException(record.exc_info)
        return base


def configure_logging(level: int = logging.INFO) -> None:
    handler = logging.StreamHandler()
    handler.setFormatter(_DevFormatter())
    root = logging.getLogger()
    root.handlers.clear()
    root.setLevel(level)
    root.addHandler(handler)


configure_json_logging = configure_logging
