FROM python:3.14-slim AS builder
WORKDIR /build
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
COPY transcoder/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

FROM python:3.14-slim AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"
RUN useradd --no-create-home --shell /bin/false app
WORKDIR /app
COPY shared/ shared/
COPY transcoder/ transcoder/
USER app
WORKDIR /app/transcoder
CMD ["python", "main.py"]
