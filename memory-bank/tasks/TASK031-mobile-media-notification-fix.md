# TASK031 - Mobile Media Notification Fix

## Status
Completed

## Original Request
Fix Android media notification bar. User reported the player notification was stuck around 59s again and the notification shade showed only the green app icon instead of the desired expanded media player notification with artwork and controls.

## Thought Process
The playback keepalive fix had added an explicit foreground-service notification in `SerenadePlayerService.onStartCommand()`. That notification used the launcher foreground asset and app name only, so Android could surface a generic app chip instead of the Media3 playback notification. Media3 also had no track metadata in its `MediaItem`s, so notification title, artwork, and duration could be missing or stale.

## Implementation Plan
1. Replace the manual generic foreground notification in the MediaSessionService.
2. Configure foreground and Media3 notifications with a proper monochrome small notification icon.
3. Pass track title, artist, album, duration, and artwork URL into Media3 `MediaMetadata`.
4. Compile Android app and update memory bank.

## Progress Tracking

### Subtasks
| ID | Description | Status |
|----|-------------|--------|
| 1 | Inspect playback service, controller, notification provider, and callsites | Complete |
| 2 | Apply Media3 notification and metadata fixes | Complete |
| 3 | Compile Android app | Complete |
| 4 | Update memory docs | Complete |

## Progress Log

### 2026-04-26
- Replaced the launcher-icon `ensureForeground()` placeholder in `SerenadePlayerService` with an immediate foreground media-style notification, avoiding Android's foreground-service timeout while keeping the notification media-shaped.
- Added `ic_notification_music.xml` and configured `DefaultMediaNotificationProvider.setSmallIcon(...)`.
- Extended `PlaybackItem` with media metadata fields.
- Updated AppNavigation queue mapping to pass track metadata into PlaybackItem.
- Added `MediaMetadata` to MediaItems with duration and artwork URI so system media controls can render artwork and a correct timeline.
- Ran `./gradlew assembleDebug --no-daemon --console=plain`; passed.
- Follow-up crash fix: restored immediate `ServiceCompat.startForeground()` after device logs showed `ForegroundServiceDidNotStartInTimeException`; recompiled successfully.
- Follow-up reactivity fix: added a `Player.Listener` in `SerenadePlayerService` that calls `triggerNotificationUpdate()` on media item, metadata, and play-state changes so notification info updates after system next/previous actions; recompiled successfully.
- Follow-up stale visible card fix: changed the listener to handle `onEvents(...)`, refresh the foreground notification itself with `ensureForeground()`, then call `triggerNotificationUpdate()`. `ensureForeground()` now reads from aggregated `player.mediaMetadata`, not only `currentMediaItem?.mediaMetadata`; recompiled successfully.
