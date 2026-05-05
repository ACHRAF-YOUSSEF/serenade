# TASK038 - Upload Studio and Downloads Design

Status: Completed

## Original Request
Also implement the Upload studio screen and the downloads screen.

## Thought Process
The app already has functional Upload and Downloads screens. This task should update the existing Compose screens to match the Serenade handoff design, preserving current upload/download behavior. Downloads currently exposes completed downloaded tracks only, so the design should show offline library/storage treatment without inventing new in-progress queue state.

## Implementation Plan
- Read memory bank and handoff Upload/Downloads prototype.
- Reskin UploadScreen with prototype-aligned Studio header, surface cards, file/artwork panels, form, genre chips, and status treatment.
- Reskin DownloadScreen with storage summary, offline cards, artwork motif, status badges, and delete actions.
- Verify Android compile and diff check.
- Update active context, progress, and task index.

## Subtasks
| Status | Item |
| --- | --- |
| Done | Read memory bank and prototype/current screens |
| Done | Update Upload and Downloads UI |
| Done | Verify Android build |
| Done | Update memory docs |

## Progress Log
- 2026-04-29: Read current memory bank, handoff `DownloadsScreen`/`UploadScreen` source, and current Compose Upload/Download implementations.
- 2026-04-29: Updated `UploadScreen` to better match the Studio prototype: compact send top action, editorial header, raised gradient audio card, artwork card, themed form fields/chips, and status card/progress treatment.
- 2026-04-29: Rebuilt `DownloadScreen` around the handoff offline design: header copy, storage/cache card, empty state, offline track cards with artwork motif, ready badges, duration text, and delete action. Kept current completed-download behavior; no new queue/progress state added.
- 2026-04-29: Verified `./gradlew --gradle-user-home ../.gradle-user-home :app:compileDebugKotlin` and `git diff --check`.
- 2026-04-29: After verification, `serenade.zip` appeared in the workspace. Read its README and Upload/Downloads source; relevant snippets match the already-used handoff design source.
