# TASK023 - B10 Observability and Request Tracing

**Status:** Completed  
**Added:** 2026-04-26  
**Updated:** 2026-04-26

## Original Request
Review the codebase, write what needs to be improved or fixed, apply those changes, move to the next phase, and update relevant markdown files such as `progress.md`.

## Thought Process
Review after TASK022 found the next highest-value phase is Backend B10 observability:
- No backend `X-Request-Id` filter or MDC context exists, so logs cannot correlate HTTP requests.
- Upload-created RabbitMQ messages do not carry request context to workers.
- Workers call backend callbacks without forwarding request context.
- Actuator exposes only health/info, leaving metrics/Prometheus unfinished.
- Backend console logging is still default text, not structured JSON.

Keep scope bounded to cross-stack request tracing and backend metrics wiring. Do not add tests because the memory bank still records the user's no-new-tests instruction.

## Implementation Plan
- Add backend request ID filter that accepts/sanitizes `X-Request-Id`, generates one when absent, writes MDC, returns the header, and logs HTTP completion metadata.
- Add request ID to upload RabbitMQ message headers.
- Add worker request-context handling from RabbitMQ headers and forward `X-Request-Id` on Spring callbacks.
- Enable Spring Boot structured JSON console logging and expose authenticated metrics/Prometheus actuator endpoints.
- Verify backend package and worker syntax.
- Update root progress and memory-bank docs.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Review findings and task docs | Complete | 2026-04-26 | B10 observability selected |
| 2 | Backend request ID + metrics patch | Complete | 2026-04-26 | RequestIdFilter, Rabbit headers, JSON logs, metrics/prometheus |
| 3 | Worker request propagation patch | Complete | 2026-04-26 | Rabbit header context + SpringClient callback header |
| 4 | Verification | Complete | 2026-04-26 | Backend package, worker compile, diff check passed |
| 5 | Progress docs | Complete | 2026-04-26 | Root and memory progress updated |

## Progress Log
- 2026-04-26: Read all memory-bank files and relevant agent instructions. Review found B10 observability gaps: no request ID/MDC filter, no Rabbit request header propagation, no worker callback trace propagation, no metrics/Prometheus exposure, and default backend text logs.
- 2026-04-26: Added backend RequestIdFilter, RabbitMQ header propagation, Spring Boot logstash JSON logging, Micrometer/Prometheus config, worker request-context extraction, JSON worker logs, and SpringClient `X-Request-Id` forwarding.
- 2026-04-26: Verified `sh mvnw -DskipTests package`, `python3 -m compileall workers/shared workers/transcoder workers/subtitler`, and `git diff --check`. Completed TASK023.
