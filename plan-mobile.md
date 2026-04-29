# Music Streaming App — Mobile Plan (Android)

Status:

Tags: android, kotlin, jetpack-compose, media3, room

---

## MVP Scope (Mobile)

User can:

- Open app, play music without signing in
- Pick biometric or password auth on first launch
- Browse, search, filter by genre
- Stream from backend OR play downloaded/local files
- Create / edit / rate playlists (authenticated)
- Upload music (authenticated) — server transcodes
- Playback continues in background with media notification
- See synced subtitles
- Add community providers via manifest URLs
- Copy another user's playlist as own

---

## Tech Stack

- Room DB + KSP
- Jetpack Compose + Navigation
- Kotlin Coroutines + Flow + ViewModel
- Hilt (DI)
- AndroidX Security (EncryptedSharedPreferences, MasterKey)
- AndroidX Biometric
- Retrofit + OkHttp + Kotlinx Serialization
- Media3 (ExoPlayer + MediaSession + MediaSessionService)
- WorkManager — downloads, sync
- Coil — artwork
- DataStore — non-sensitive settings
- Accompanist Permissions

---

## Module Layout

```
:app
:core:designsystem
:core:common
:core:database    (Room, local cache)
:core:datastore   (settings)
:core:network     (Retrofit, interceptors)
:core:security    (crypto, biometric gate)
:core:player      (Media3 service + controller)
:core:providers   (provider SPI + manifest loader)
:feature:auth
:feature:home
:feature:search
:feature:player
:feature:playlist
:feature:library
:feature:upload
:feature:providers
:feature:settings
```

---

## Feature M1: Project Setup + Room DB Foundation

Everything else builds on this.

**Steps:**

1. Create Android project — Empty Activity, Kotlin, API 26, Compose
2. Add dependencies above; enable KSP; Hilt plugin
3. Create module layout
4. Create Room entities:

   > **ID convention (all Room entities):** PKs are `String` (UUID stored as TEXT). Generate via `UUID.randomUUID().toString()`. No `@Autoincrement`. FK columns typed `String`.
   - `TrackEntity` — id: String (UUID, Room PK), remoteId: String? (server UUID), title, artist, album, genre (enum), durationMs, artworkUrl, localPath (nullable), streamUrl (nullable), isDownloaded, providerId: String (UUID), updatedAt
   - `PlaylistEntity` — id: String (UUID, Room PK), remoteId: String? (server UUID), name, ownerId: String (UUID), isMine, isCopy, sourcePlaylistId: String? (UUID), ratingAvg, updatedAt
   - `PlaylistTrackCrossRef` — playlistId: String (UUID), trackId: String (UUID), position: Int
   - `SubtitleLineEntity` — id: String (UUID), trackId: String (UUID), startMs, endMs, text
   - `DownloadEntity` — id: String (UUID), trackId: String (UUID), state, progress, filePath
   - `PendingOpEntity` — id: String (UUID, client-generated), type, payloadJson, createdAt (outbox)
   - `ProviderEntity` — id: String (UUID), manifestUrl, name, version, capabilities, enabled
   - `UserEntity` — id: String (UUID), username, email, tokenEnc, refreshEnc (nullable if anon)
   - `RatingEntity` — id: String (UUID), targetType (TRACK/PLAYLIST), targetId: String (UUID), value, syncedAt

5. DAOs (Flow reads): `TrackDao`, `PlaylistDao`, `SubtitleDao`, `DownloadDao`, `PendingOpDao`, `ProviderDao`, `UserDao`, `RatingDao`
6. `AppDatabase` with type converters (Genre enum, Instant)
7. `Genre` enum — POP, ROCK, HIPHOP, RNB, ELECTRONIC, CLASSICAL, JAZZ, METAL, FOLK, COUNTRY, LATIN, OTHER
8. Test — insert fake track/playlist, read back

**Done when:** DB reads/writes verified via unit test.

---

## Feature M2: Startup Auth Gate (Biometric or Password)

First launch asks: biometric or regular auth. Anonymous allowed.

**Steps:**

1. `AuthPreference` in DataStore — `authMode` ∈ { NONE, PASSWORD, BIOMETRIC }, `skippedAuth` bool
2. `WelcomeScreen` shown on first launch only:

   - "Sign in with biometric"
   - "Sign in with password"
   - "Skip — browse anonymously"

3. `AuthViewModel`:

   - Biometric: check `BiometricManager.canAuthenticate()`. Not enrolled → disable button with hint
   - Password: email/password flow → backend `/auth/login` → JWT (access + refresh)

4. `SecureTokenStore` — store refresh token + derived key in `EncryptedSharedPreferences` backed by `MasterKey` (StrongBox when available)
5. Biometric unlock flow:

   - `BiometricPrompt` gates decryption via `CryptoObject` (AES key in Keystore, `setUserAuthenticationRequired(true)`)
   - Success → decrypt refresh → silent refresh access
   - Skip prompt on later launches only if toggled ON and user previously opted in

6. Guard: mutations (create/edit/delete playlist, upload, rate) check `authState.isAuthenticated`; false → prompt login

**Done when:** First launch picks mode → later launches auto-unlock per choice → protected actions gated.

---

## Feature M3: Player Core (Media3 + Background + Notification)

**Steps:**

1. `MusicService : MediaSessionService`:

   - Holds `ExoPlayer` + `MediaSession`
   - Foreground service with `mediaPlayback` type (required API 34+)
   - Perms: `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS`
   - `MediaLibrarySession` for queue, next/prev, seek, shuffle, repeat

2. `PlayerRepository` — exposes `MediaController` flow to UI
3. Notification auto-handled by Media3
4. Source resolver: if `track.localPath` present or `isDownloaded` → local file; else stream URL (HLS via `HlsMediaSource.Factory`)
5. Audio focus, become-noisy, headset events (Media3 default)
6. Background rule: promote to foreground while `isPlaying`; demote on pause > N min or stop
7. `NowPlayingScreen` + mini player — bind to `MediaController`

**Done when:** App swiped while playing → music continues → notification works → reopening restores state.

---

## Feature M4: Browse, Search, Filter by Genre

**Steps:**

1. `HomeScreen` — featured playlists, recently played, trending (from backend, cached to Room)
2. `SearchScreen`:

   - Query field (debounced 300 ms)
   - Genre filter chip row from `Genre` enum
   - Results: tracks + playlists tabs
   - Backend `GET /search?q=&genre=`

3. Offline fallback — query Room when no network
4. `SearchViewModel` uses `stateIn` + `SavedStateHandle`

**Done when:** Typing returns results, genre chips filter, offline shows cached.

---

## Feature M5: Playlists (Create / Edit / Copy / Rate)

**Steps:**

1. `LibraryScreen` lists own + saved/copied playlists
2. `PlaylistDetailScreen` — track list with drag-to-reorder, add/remove, play-all, shuffle toggle
3. **Intelligent shuffle** (no AI):

   - Fisher–Yates with constraints:

     - No two consecutive tracks by same artist
     - Genre clustering reduced (weighted spread)
     - Previously played in last K items demoted

   - In `:core:player` as `ShuffleOrder` custom impl

4. **Copy playlist**: `POST /playlists/{id}/copy` → clone server-side → mirror to Room with `isCopy=true`, `sourcePlaylistId=original`
5. **Rating**: 1–5 stars, `RatingEntity` local + `POST /ratings`; show aggregate avg
6. All mutations require auth

**Done when:** Create → add tracks → reorder → copy someone's → edit copy → rate → see avg update.

---

## Feature M6: Downloads + Offline Playback

**Steps:**

1. `DownloadScreen` / download button on track row
2. `DownloadWorker` (WorkManager, `expedited` if allowed):

   - Fetch stream URL → download to app-private storage
   - Update `DownloadEntity` progress → Flow drives UI
   - On finish → `TrackEntity.isDownloaded=true`, `localPath=...`
   - Notification "Download complete: {title}" (separate channel from playback)

3. Player source resolver already prefers local (M3)
4. Delete download — remove file + flags

**Done when:** Download track → airplane mode → play from Library.

---

## Feature M7: Upload (Mobile Side)

**Steps:**

1. `UploadScreen` — SAF file picker, metadata fields (title, artist, album, genre enum), artwork picker
2. `POST /uploads` — multipart (progress via OkHttp listener)
3. Response: `uploadId`, `status=PROCESSING`
4. Poll or SSE `GET /uploads/{id}` until `READY` → track appears in library

**Done when:** Upload MP3 → backend returns READY → track plays via HLS.

---

## Feature M8: Subtitles Overlay

**Steps:**

1. `GET /tracks/{id}/subtitles` → list of lines
2. Cache to Room on first play
3. `SubtitleOverlay` Composable subscribes to `player.currentPositionMs` (200 ms poll via `flow`) → highlights active line, auto-scrolls
4. Empty response → show "Generating…" banner; poll/SSE until ready

**Done when:** Playing track with subtitles scrolls in sync; uploaded-without-lyrics track eventually gets AI-generated.

---

## Feature M9: Sync Strategy (Local ↔ Remote)

Outbox + last-write-wins with version vectors for playlists.

**Rules:**

1. All mutations write Room first, then insert `PendingOpEntity`. UI updates instantly
2. `SyncWorker` (periodic + on-connectivity) drains outbox, calls backend, marks applied
3. Each server entity carries `updatedAt` + `version` (monotonic int). Pull via `GET /changes?since=cursor`
4. Conflict rules:

   - **Ratings** — last-write-wins
   - **Playlist metadata (name, order)** — server `version` authoritative; stale local op → server rejects → client re-fetches, re-applies, retries. Rare merge conflicts surfaced to user
   - **Track metadata** — server authoritative (users don't edit post-upload in MVP)
   - **Uploads / deletes** — idempotent via client UUIDs

5. Anonymous keeps local; on login, outbox flushes under new userId

**Done when:** Edit offline → reconnect → change propagates; concurrent edits resolved.

---

## Feature M10: Community Providers (Mobile Side)

**Manifest (`provider.json`):**

```json
{
  "id": "com.example.spotify-bridge",
  "name": "Spotify Bridge",
  "version": "1.0.0",
  "author": "...",
  "capabilities": ["search", "stream", "playlists"],
  "authType": "oauth2",
  "authConfig": { "authUrl": "...", "tokenUrl": "...", "scopes": ["..."] },
  "baseUrl": "https://provider.example.com/api",
  "endpoints": {
    "search": "/search?q={q}&genre={genre}",
    "track":  "/tracks/{id}",
    "stream": "/tracks/{id}/stream"
  },
  "signature": "..."
}
```

**Steps:**

1. `:core:providers` defines `MusicProvider` SPI: `search()`, `getTrack()`, `getStreamUrl()`, `listPlaylists()` — suspend, common DTOs
2. `ProviderRegistry` keyed by `ProviderEntity.id`; `BackendProvider` built-in, always enabled
3. `ProviderManifestLoader` — user pastes URL → download JSON → validate schema + signature → store Room → instantiate `ManifestProvider` (data-driven Retrofit client reading endpoints from manifest)
4. `ProvidersScreen` — list / add / toggle / remove
5. OAuth via Chrome Custom Tabs; tokens in `EncryptedSharedPreferences` per-provider
6. Search + browse aggregate across enabled providers; origin tagged by `providerId`
7. Self-hosted = same mechanism, any HTTPS endpoint that honors SPI

**Security:**

- Manifest signature check against allow-list of public keys OR user-accept-on-add (shows capabilities)
- MVP: data-only providers (no code exec) — hardened Retrofit with strict timeouts + response-size caps

**Done when:** Add manifest URL → provider appears → search returns results → play track from it.

---

## What Mobile MVP Demonstrates

| Concept                     | Where                                         |
| --------------------------- | --------------------------------------------- |
| Room + KSP                  | Track, Playlist, Subtitle, PendingOp, Rating  |
| Reactive UI                 | Flow → Compose for library, downloads, player |
| Hilt DI                     | All repos, services, use cases                |
| AndroidX Security           | Token + provider-credential storage           |
| Biometric                   | Gated refresh-token decryption                |
| Media3 background playback  | Foreground service + notification             |
| WorkManager                 | Downloads, sync                               |
| Retrofit                    | Backend + manifest-driven providers           |
| Offline-first + sync        | Outbox + versioned pull                       |
| Modularity                  | Gradle modules + provider SPI                 |

---

## Post-MVP (Mobile)

| Addition                      | Value                                    |
| ----------------------------- | ---------------------------------------- |
| Social (follow, feed)         | Network effects                          |
| Collaborative playlists       | Real-time via WebSocket                  |
| Lyrics karaoke mode           | Engagement                               |
| Recommendations               | Retention                                |
| Cast to Chromecast            | Polish                                   |
| Equalizer                     | Polish                                   |
| Android Auto                  | Reach                                    |
