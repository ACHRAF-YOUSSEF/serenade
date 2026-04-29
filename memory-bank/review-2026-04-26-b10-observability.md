# Code Review — 2026-04-26 B10 Observability

## Needs Fixing
- Backend has no `X-Request-Id` filter, no MDC request context, and no request completion log metadata.
- Upload RabbitMQ jobs do not carry request context, so worker logs and callbacks cannot be correlated to the originating HTTP upload.
- Workers do not read request IDs from RabbitMQ headers or forward them to Spring Boot internal callbacks.
- Actuator exposes only `health` and `info`; B10 still needs metrics/Prometheus wiring.
- Backend console logging uses the default text pattern instead of structured JSON.

## Selected Phase
B10 observability. Implement request tracing across HTTP → RabbitMQ → workers → internal callback, plus metrics/Prometheus actuator config.

## Fixed This Pass
- Added backend `RequestIdFilter` for `X-Request-Id` validation/generation, MDC context, response header echo, and request completion logs.
- Propagated request IDs from upload HTTP requests into RabbitMQ message headers.
- Added worker request context handling from RabbitMQ headers and forwarded `X-Request-Id` on Spring internal callbacks.
- Switched backend console logging to Spring Boot logstash JSON.
- Exposed authenticated `/actuator/metrics` and `/actuator/prometheus`; added Prometheus registry dependency, Micrometer app tags, and HTTP server histograms.

## Still Needs Work
- No OpenTelemetry exporter or distributed tracing backend is configured.
- Rate limiting still uses in-memory Bucket4j buckets.
- Worker retry/backoff before DLQ remains pending.
