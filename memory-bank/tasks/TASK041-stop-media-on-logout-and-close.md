# TASK041 - Stop Media on Logout and App Close

Status: Completed

## Original Request
Fix bug where media keeps playing after logout and when closing the app.

## Thought Process
Playback is app-scoped through a singleton ExoPlayer plus a MediaSessionService. Logout cleared tokens only, so the player and foreground service kept running. App task removal also did not tell the service to stop playback.

## Implementation Plan
- Add an explicit `PlayerController.stopPlayback()` API.
- Clear persisted playback queue only on logout for account/privacy cleanup.
- Stop current player/service when Android removes the app task.
- Stop playback when the main activity is explicitly finishing.
- Verify Android compile and diff check.

## Subtasks
| Status | Item |
| --- | --- |
| Completed | Add player stop API |
| Completed | Wire logout to stop playback and clear queue |
| Completed | Stop service playback on task removal/app finish |
| Completed | Verify Android compile |

## Progress Log
- 2026-04-29: Added `PlaybackRepository.clearQueue()` and `PlayerController.stopPlayback(clearPersistedQueue)`.
- 2026-04-29: Logout now stops playback, clears in-memory queue, clears persisted playback queue, then clears auth state.
- 2026-04-29: `SerenadePlayerService.onTaskRemoved()` pauses/stops/clears ExoPlayer, removes foreground notification, and stops itself when the app task is swiped away.
- 2026-04-29: `MainActivity.onDestroy()` stops playback when the activity is finishing. Verified `./gradlew :app:compileDebugKotlin`.
