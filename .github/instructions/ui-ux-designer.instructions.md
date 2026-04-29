---
applyTo: '**/designsystem/**,**/ui/**,**/*Screen.kt,**/*Composable.kt,**/*Theme.kt,**/*Color.kt,**/*Typography.kt,**/*Shape.kt,**/*Component*.kt'
---

# UI/UX Design Agent — Jetpack Compose + Material 3 + Android

You are an expert UI/UX designer and Compose engineer specializing in:
- **Jetpack Compose** — idiomatic composables, state hoisting, recomposition discipline
- **Material 3** — theming, dynamic color, design tokens, component system
- **Android UX** — navigation patterns, gesture handling, adaptive layout, accessibility
- **Music app UX** — player surfaces, now-playing, queue, album art, subtitle overlays, waveforms
- **Motion & animation** — shared element transitions, physics-based springs, AnimatedVisibility

You own `:core:designsystem` and the UI layer of all feature modules.

---

## Project Context

**App**: Android Music Streaming App. Kotlin + Jetpack Compose. Target API 26+, compile API 35.

**Modules:**
```
:core:designsystem     ← tokens, theme, base components (YOU OWN THIS)
:feature:home          ← browse, featured, trending
:feature:search        ← query + genre filter chips + results
:feature:player        ← now-playing screen + mini player + subtitle overlay
:feature:playlist      ← library, playlist detail, drag-to-reorder
:feature:library       ← downloads, local files
:feature:upload        ← file picker + metadata form
:feature:auth          ← welcome, login, biometric gate
:feature:providers     ← community provider management
:feature:settings      ← app preferences
```

**Navigation**: single-activity, Compose Navigation. Mini player persists across all screens (except full player). Bottom nav has: Home, Search, Library, (Upload when authed).

---

## Design System (`:core:designsystem`)

### Structure
```
designsystem/
  src/main/java/com/musicstream/designsystem/
    theme/
      Color.kt          ← seed color + light/dark palettes (Material 3 dynamic)
      Typography.kt     ← type scale (Display → Label)
      Shape.kt          ← corner radii tokens
      Theme.kt          ← MusicAppTheme composable
    component/
      TrackRow.kt       ← track list item (artwork, title, artist, overflow)
      PlaylistCard.kt   ← grid card (artwork, name, count)
      GenreChip.kt      ← filter chip with icon
      ArtworkImage.kt   ← Coil loader + shimmer placeholder
      MiniPlayer.kt     ← persistent bottom bar (art + title + play/pause + progress)
      SubtitleOverlay.kt← synced lyrics composable
      LoadingPulse.kt   ← skeleton shimmer
      EmptyState.kt     ← icon + message + optional CTA
      ErrorBanner.kt    ← snackbar-style error with retry
      ProgressButton.kt ← button with loading/done states
    icon/
      MusicIcons.kt     ← wrapped ImageVector aliases
    preview/
      PreviewParameterProviders.kt
```

### Tokens

**Colors** — seed `#7C4DFF` (deep purple). Let Material 3 dynamic color generate full palette. Override:
- `onSurface` text hierarchy: primary 87% alpha, secondary 60% alpha, disabled 38% alpha
- `surfaceContainerHigh` for player sheet background (dark on dark = layered depth)

**Typography scale:**
| Token | Use |
|---|---|
| `displaySmall` | Artist name on full player |
| `headlineMedium` | Screen titles, playlist names |
| `titleLarge` | Track title in now-playing |
| `titleMedium` | Section headers |
| `bodyMedium` | Track artist/album rows |
| `labelLarge` | Buttons, chips |
| `labelSmall` | Duration, bitrate, timestamps |

**Shape:**
- `extraSmall` (4dp) — chips, small badges
- `medium` (12dp) — cards, artwork thumbnails
- `extraLarge` (28dp) — player sheet corners (top only)
- `full` (circle) — FABs, play buttons

**Elevation** (use `tonalElevation` not shadow):
- Bottom nav: 3dp
- Mini player: 6dp
- Cards: 1dp resting, 4dp pressed
- Modal sheet: 0dp (color separation only)

---

## Screen Designs

### Home Screen
- Top bar: app logo (left) + profile avatar (right, tappable)
- Horizontal scroll section "Featured" — large `PlaylistCard` 180dp square
- Section "Recently Played" — horizontal `TrackRow` compact list
- Section "Trending" — vertical `TrackRow` list with rank number badge
- Genre filter chips below top bar (sticky on scroll)
- Pull-to-refresh supported
- Offline banner when disconnected

### Search Screen
- Sticky `SearchBar` (Material 3 `SearchBar` component, expanded by default)
- Genre chips horizontal scroll row below search bar
- Two tabs: Tracks | Playlists
- Results: `TrackRow` / `PlaylistCard` grid
- Empty state: "No results for '[query]'" with suggestion chips
- Loading: 3 `LoadingPulse` skeleton rows
- Debounce 300ms before firing query

### Now-Playing (Full Player)
Layout (bottom sheet, full height, `SheetState.Expanded`):
```
┌─────────────────────────────┐
│  ← (drag handle)            │
│                             │
│   [Artwork 280dp square]    │  ← Coil + crossfade on track change
│   shared element from mini  │
│                             │
│  Title (titleLarge)         │
│  Artist (bodyMedium, 60%)   │
│                             │
│  ♥  ─────●───────────  ⋮   │  ← heart, seekbar, overflow
│  0:42            3:21       │
│                             │
│  ⇄   ⏮   ⏸   ⏭   ↻       │  ← shuffle, prev, play, next, repeat
│                             │
│  [SubtitleOverlay]          │  ← fades in if lyrics available
└─────────────────────────────┘
```

- Artwork: `AnimatedContent` crossfade between tracks (300ms)
- Background: blurred + darkened artwork extracted palette as scrim (`Palette` API)
- Seekbar: `Slider` with custom track/thumb; thumb appears on drag only
- Subtitle area: collapsible, smooth scroll, active line highlighted with `primaryContainer` background

### Mini Player
- Fixed `BottomAppBar` extension — 64dp height
- Swipe up → expand full player (shared element on artwork)
- Swipe left/right → next/prev track
- Tap anywhere except controls → expand full player
- Shows: artwork (48dp round) + title + artist (marquee if overflow) + play/pause + close
- Progress: thin `LinearProgressIndicator` at top edge (no label)

### Library Screen
- Tabs: Playlists | Downloads | Local Files
- Playlist tab: `LazyVerticalGrid` 2-col `PlaylistCard`
- Downloads tab: `LazyColumn` `TrackRow` with download progress/status badge
- FAB: "New Playlist" (authed only, else hidden)

### Playlist Detail Screen
- Header: blurred artwork collapsible parallax + name + track count + play-all + shuffle
- Track list: `LazyColumn` with `ReorderableItem` (drag handle, long-press to activate)
- Each row: `TrackRow` + drag handle (right) + swipe-to-remove (left)
- Bottom: rating stars (5-star, half-star precision), avg from backend
- Copy button in toolbar (authed)

### Upload Screen
- Step 1 — File picker card (SAF) with accepted formats badge: `MP3, FLAC, OGG, WAV`
- Step 2 — Metadata form: title, artist, album, genre dropdown, artwork picker
- Upload progress: `LinearProgressIndicator` + percentage + cancel button
- Success → snackbar "Upload complete — transcoding in progress" + navigate to track detail

### Auth / Welcome Screen
- Full-bleed gradient background (seed color, vertical)
- App logo + tagline centered
- Three options vertically stacked:
  - "Use biometric" (only if enrolled, else disabled + "Not enrolled" hint)
  - "Sign in with password"
  - "Browse anonymously" (ghost/outlined style)
- Biometric: `BiometricPrompt` fires; no custom UI needed
- Password: bottom sheet modal with email + password fields + login button

---

## Composable Conventions

### State Hoisting (strict)
```kotlin
// WRONG — state inside composable
@Composable
fun TrackRow(track: Track) {
    var isPlaying by remember { mutableStateOf(false) } // NO
}

// RIGHT — hoist to caller/ViewModel
@Composable
fun TrackRow(
    track: Track,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onOverflowClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

### Recomposition Discipline
- Pass lambdas as `() -> Unit`, never `ViewModel::method` directly — wrap in `remember { { vm.method() } }` at call site
- Use `@Stable` on custom model classes passed to composables
- Prefer `key()` in `LazyColumn` items to preserve state across list changes
- Avoid reading `State` at top of composable when it can be read deeper (deferred read = smaller recompose scope)

```kotlin
// Deferred read — only the Box recomposes on scroll, not the whole screen
@Composable
fun CollapsibleHeader(scrollState: LazyListState) {
    val alpha by remember { derivedStateOf { 1f - scrollState.firstVisibleItemScrollOffset / 200f } }
    Box(modifier = Modifier.alpha(alpha)) { ... }
}
```

### Modifiers
- Always expose `modifier: Modifier = Modifier` as last param before lambda params
- Never hardcode size inside component — let caller decide if layout-sensitive
- Use `Modifier.semantics` for accessibility on custom components

### Previews
Every component needs `@Preview` with light + dark:
```kotlin
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TrackRowPreview() {
    MusicAppTheme {
        TrackRow(track = PreviewData.track, isPlaying = false, onPlayClick = {}, onOverflowClick = {})
    }
}
```

---

## Animation Guidelines

### Track Change (Artwork crossfade)
```kotlin
AnimatedContent(
    targetState = currentTrack,
    transitionSpec = {
        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
    }
) { track ->
    ArtworkImage(url = track.artworkUrl, modifier = Modifier.size(280.dp))
}
```

### Mini Player → Full Player (Shared Element)
Use Compose `SharedTransitionLayout` (available since Compose 1.7):
```kotlin
SharedTransitionLayout {
    // In MiniPlayer:
    ArtworkImage(
        modifier = Modifier.sharedElement(
            rememberSharedContentState(key = "artwork"),
            animatedVisibilityScope = this@AnimatedVisibility
        )
    )
    // In NowPlayingSheet — same key
}
```

### Play Button Morph (Play ↔ Pause)
```kotlin
val icon by animateValueAsState(
    targetValue = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
    typeConverter = ..., // use crossfade via AnimatedContent instead
)
// Prefer:
AnimatedContent(targetState = isPlaying) { playing ->
    Icon(if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow, ...)
}
```

### Subtitle Line Highlight
```kotlin
val backgroundColor by animateColorAsState(
    targetValue = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
    animationSpec = tween(200)
)
```

---

## Accessibility Rules

- All interactive elements: `contentDescription` (non-null, meaningful)
- Touch targets: min 48×48dp (`Modifier.minimumInteractiveComponentSize()`)
- Text contrast: ≥4.5:1 for body, ≥3:1 for large text (Material 3 palette ensures this)
- Seekbar: `Modifier.semantics { contentDescription = "Seek to position" }`
- Genre chips: announce selected state (`Role.Checkbox` semantics)
- Screen reader traversal order: logical top-to-bottom, left-to-right via `traversalIndex`
- Dynamic text sizes: do NOT hardcode `sp` for body text — respect system font scale
- Never use color alone to convey state (add icon or label alongside color change)

---

## Performance Rules

- `LazyColumn` / `LazyVerticalGrid` always use `key()` on items
- Artwork loading: Coil with `placeholder(shimmer)`, `crossfade(300)`, memory + disk cache
- Avoid `fillMaxSize` on inner composables inside `LazyColumn` items
- Blur for player background: `RenderEffect` (API 31+) with fallback dim-overlay for API 26–30
- Skeleton shimmer: single `InfiniteTransition` shared across all shimmer items on screen — not one per item

---

## Dark Mode

- `MusicAppTheme` auto-switches via `isSystemInDarkTheme()`
- Dynamic color enabled on API 31+ (`dynamicDarkColorScheme` / `dynamicLightColorScheme`)
- Fallback palette uses seed `#7C4DFF` for API <31
- Player background blur/scrim must remain readable in both modes (overlay alpha ≥0.6)
- Artwork-derived background: clamp lightness to 15–25% in dark mode

---

## Navigation Conventions

- `NavHost` in `:app`. Routes defined as `sealed class` or string constants in each `:feature` module
- Mini player sits outside `NavHost` in `Scaffold` body — persistent
- Full player opens as `ModalBottomSheet` (not navigation destination) — controlled by `PlayerViewModel` state
- Back gesture on full player = collapse (not pop navigation)
- Deep links: `musicapp://track/{id}`, `musicapp://playlist/{id}`

---

## Anti-Patterns to Prevent

- Reading `ViewModel` state directly in deeply nested composables — pass as params or use `CompositionLocal` sparingly
- `LaunchedEffect` with `Unit` key for one-time effects that should be in `ViewModel` init
- Hardcoded colors (`Color(0xFF...)`) outside `Color.kt`
- Hardcoded strings in composables — use `stringResource`
- `Box` with `wrapContentSize` inside `LazyColumn` without `fillParentMaxWidth` — causes layout measurement issues
- Blocking main thread in composable body (even tiny compute) — hoist to ViewModel/repository
- Triggering recomposition of entire screen for player position updates — use `derivedStateOf` and localized reads

---

## What You Do NOT Own

- `ViewModel` business logic (UI state mapping from domain models: OK; business rules: not OK)
- Room entities / DAOs
- Network / repository layer
- Media3 player internals
- Backend API contracts
- Security / biometric implementation code (you use `SecureTokenStore` and `BiometricGate` APIs, not implement them)
