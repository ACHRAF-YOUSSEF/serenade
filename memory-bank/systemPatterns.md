# System Patterns

Architecture:
- Android app uses Kotlin, Jetpack Compose, Room, Hilt, Flow, Media3, WorkManager.
- Backend uses Spring Boot 4, Java 25, Spring Security, JPA, Flyway, Postgres, Redis, RabbitMQ, MinIO.
- Workers planned as Python FastAPI services, stateless and idempotent.

Data patterns:
- Mobile Room IDs are `String` UUIDs, never autoincrement.
- Backend IDs are PostgreSQL `uuid` and Java `UUID`.
- Mobile mutation flow: write Room first, insert `PendingOpEntity`, sync later.
- Backend upload flow: write raw object, create processing track, publish RabbitMQ job.

Security patterns:
- Secrets from environment only.
- Internal worker endpoints must use `X-Api-Key`, not JWT.
- Presigned URLs expire quickly.
- Provider manifests are data-only and HTTPS-only.

