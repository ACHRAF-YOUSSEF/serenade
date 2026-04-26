# TASK002 - Apply Room Review Fixes

**Status:** Completed  
**Added:** 2026-04-25  
**Updated:** 2026-04-25

## Original Request
Read `memory-bank/review-2026-04-25.md` and apply the fixes.

## Thought Process
The review targets the Android Room layer. Fix critical/high safety issues first: no sensitive tokens in SQLite, safe enum converters, migration fallback for development, schema export, Room foreign keys/indexes, playlist track join query, provider HTTPS guard. Apply medium type-safety fixes when low-risk. Do not add tests because the user previously instructed not to write tests.

## Implementation Plan
- Update Room entities and converters.
- Add schema export configuration and development migration fallback.
- Add repository/DAO guard helpers for provider URLs and rating bounds.
- Verify Android build without tests.
- Update progress and memory bank.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Read review and affected Android files | Done | 2026-04-25 | Review loaded |
| 2 | Patch Room model/converters/schema config | Done | 2026-04-25 | Critical/high fixes plus low-risk medium fixes |
| 3 | Verify build without tests | Done | 2026-04-25 | Android assemble passed |
| 4 | Update memory/progress | Done | 2026-04-25 | Progress docs updated |

## Progress Log
- 2026-04-25: Started TASK002. Review findings mapped to Android Room files. Tests intentionally excluded per user instruction.
- 2026-04-25: Removed auth tokens from Room user profile and added `SecureTokenStore` backed by `EncryptedSharedPreferences`.
- 2026-04-25: Hardened converters, schema export, dev migration fallback, Room FK/indexes, playlist JOIN query, provider HTTPS repository guard, typed provider capabilities/pending ops, stream URL expiry, and rating bounds.
- 2026-04-25: Verified with `./gradlew --gradle-user-home ../.gradle-user-home assembleDebug -x test`. Build passed. Remaining intentional gaps: no tests per user instruction; no FK on polymorphic `RatingEntity.targetId`.
