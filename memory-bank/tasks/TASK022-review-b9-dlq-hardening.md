# TASK022 - Review B9/DLQ Hardening

**Status:** Completed  
**Added:** 2026-04-26  
**Updated:** 2026-04-26

## Original Request
Review the codebase, write what needs to be improved or fixed, apply those changes, move to the next phase, and update relevant markdown files such as `progress.md`.

## Thought Process
The highest-risk review findings are backend/worker reliability and security hardening:
- RabbitMQ DLQ queues exist, but main queues are not declared with `x-dead-letter-exchange`, so failed jobs do not route to DLQ.
- Workers process messages with `requeue=True`, causing processing failures to loop instead of dead-lettering.
- Rate limiting returns 429 without `Retry-After` and trusts `X-Forwarded-For` from any client.
- CORS is wildcard-based; B9 calls for an explicit allow-list from config.
- BCrypt uses default strength instead of the project security target.
- Project docs drifted: root README/progress are stale and TASK021 file was missing.

## Implementation Plan
- Patch RabbitMQ queue declarations with DLX and routing keys.
- Patch worker processing to NACK processing failures without requeue.
- Harden rate limiting with endpoint policies, trusted proxy handling, and `Retry-After`.
- Move CORS origins/trusted proxies into `application.yaml`.
- Raise BCrypt strength.
- Document findings and update root/memory progress docs.
- Verify backend package, worker syntax, and diff hygiene.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Review findings + task docs | Complete | 2026-04-26 | B9/DLQ hardening selected |
| 2 | Backend hardening patch | Complete | 2026-04-26 | SecurityConfig, RateLimitFilter, RabbitConfig, MinioService |
| 3 | Worker message ACK patch | Complete | 2026-04-26 | Transcoder/subtitler use no-requeue on processing failure |
| 4 | Markdown updates | Complete | 2026-04-26 | Review note, progress, active context, README |
| 5 | Verification | Complete | 2026-04-26 | Backend package, worker compile, diff check |

## Progress Log
- 2026-04-26: Read all memory-bank files and relevant agent instructions. Review found DLQ wiring mismatch, worker infinite requeue risk, rate-limit header/proxy gaps, wildcard CORS, BCrypt default strength, stale docs, and missing TASK021 file.
- 2026-04-26: Patched RabbitMQ DLX arguments, worker `requeue=False`, endpoint-specific rate policies with `Retry-After`, trusted-proxy handling for `X-Forwarded-For`, YAML CORS allow-list, BCrypt strength 12, authenticated Swagger/OpenAPI access, and MinIO expiry lower bound.
- 2026-04-26: Updated review note, task docs, root progress, memory progress, active context, and README. Verification completed with backend package, Python compile, and diff check.
