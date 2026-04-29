# Code Review — 2026-04-26 B9/DLQ Hardening

## Fixed This Pass
- RabbitMQ DLQ wiring was incomplete: DLQ queues existed, but the main transcoder/subtitler queues did not declare `x-dead-letter-exchange` or routing keys. Added DLX arguments to both main queues.
- Workers used `message.process(requeue=True)` for processing failures, which could loop failed jobs indefinitely. Changed transcoder and subtitler consumers to NACK without requeue so failed jobs route to DLQ.
- Rate limiting returned 429 without `Retry-After`. Added endpoint-specific policies and `Retry-After` plus `X-Rate-Limit-Remaining`.
- Rate limiting trusted `X-Forwarded-For` from any caller. It now uses forwarded IPs only when the direct peer is in `app.rate-limit.trusted-proxies`.
- CORS used wildcard origin patterns. It now uses explicit `app.cors.allowed-origins` from YAML.
- BCrypt used default strength. Raised to strength 12.
- Swagger/OpenAPI endpoints were publicly permitted. They now fall under normal authenticated access.
- Presigned URL expiry now clamps to 1–15 minutes instead of only enforcing the upper bound.
- Memory bank drift fixed by creating the missing TASK021 file and adding TASK022.

## Still Needs Work
- Rate limiting is still in-memory; distributed deployments still need Bucket4j Redis/Lettuce proxy manager.
- Worker DLQ is immediate on processing failure; retry/backoff policy is still pending.
- Existing RabbitMQ queues must be deleted/redeclared once for new DLX arguments to apply.
- Refresh token rotation/blacklist remains pending.
- Request ID/MDC structured logging and Prometheus metrics remain B10 work.
