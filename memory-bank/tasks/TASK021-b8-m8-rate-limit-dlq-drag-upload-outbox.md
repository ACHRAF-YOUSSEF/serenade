# TASK021 - B8/M8 Rate Limiting + DLQ + Drag Reorder + Offline Upload Outbox

**Status:** Completed  
**Added:** 2026-04-26  
**Updated:** 2026-04-26

## Original Request
Continue the next build phase after worker admin API/security playlist work and update the relevant progress docs.

## Thought Process
This task file was reconstructed because `tasks/_index.md` already listed TASK021 as completed, but the task file was missing. Current memory and root progress show the completed slice: backend B8 rate limiting and RabbitMQ DLQ declarations, plus Android M8 playlist reorder and offline upload outbox work.

## Implementation Plan
- Add backend rate limiting and CORS/DLQ wiring.
- Add Android playlist reorder and upload outbox support.
- Fix playlist rating state load.
- Update progress docs.

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks
| ID | Description | Status | Updated | Notes |
|----|-------------|--------|---------|-------|
| 1 | Backend rate limiting/CORS/DLQ | Complete | 2026-04-26 | In-memory Bucket4j and DLQ queues added |
| 2 | Mobile playlist reorder | Complete | 2026-04-26 | Up/down row controls + outbox op |
| 3 | Offline upload outbox | Complete | 2026-04-26 | Pending file copy + UPLOAD_TRACK op |
| 4 | Rating state fix | Complete | 2026-04-26 | `RatingDao.getByTargetOnce` used |
| 5 | Docs | Complete | 2026-04-26 | Memory/root progress updated |

## Progress Log
- 2026-04-26: Reconstructed task file from `activeContext.md`, `memory-bank/progress.md`, root `progress.md`, and `_index.md` because the index referenced TASK021 but no task file existed.
