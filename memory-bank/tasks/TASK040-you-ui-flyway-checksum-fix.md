# TASK040 - You UI and Flyway Checksum Fix

Status: Completed

## Original Request
Fix the UI bug shown on the You screen screenshots after the email-auth work also exposed a backend Flyway checksum mismatch.

## Thought Process
The You screen was always scrollable even when its content fit, which could expose a right-edge scroll indicator. The shared background also used `Float.MAX_VALUE` as a radial-gradient center, which is risky for GPU rendering and can create edge artifacts. The backend crash was caused by editing an already-applied V2 migration.

## Implementation Plan
- Restore V2 migration to the checksum already applied in local Postgres.
- Add V3 migration for the later auth-code index change.
- Remove forced scrolling from the fitted You screen.
- Replace infinite gradient center with a finite offscreen center.
- Verify backend package, Android compile, and diff whitespace.

## Subtasks
| Status | Item |
| --- | --- |
| Completed | Restore immutable V2 migration and add V3 follow-up |
| Completed | Fix You screen right-edge visual artifact |
| Completed | Verify backend package and Android compile |
| Completed | Update memory docs |

## Progress Log
- 2026-04-29: Restored `V2__auth_email_codes.sql` unique index definition to match applied Flyway checksum and added `V3__relax_auth_code_hash_index.sql` to drop/recreate the index as non-unique.
- 2026-04-29: Removed `verticalScroll` from `YouScreen` so the fitted settings page no longer shows a right-side scroll thumb.
- 2026-04-29: Replaced `Offset(Float.MAX_VALUE, 0f)` in `SrScreenBackground` with finite `Offset(1200f, 0f)` to avoid right-edge render artifacts.
- 2026-04-29: Verified `sh mvnw -DskipTests package` and `./gradlew :app:compileDebugKotlin`. `spring-boot:run` in sandbox reached startup but DB socket access was blocked by sandbox (`Operation not permitted`), not by Flyway checksum.
