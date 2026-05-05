# TASK042 - Theme Preview Colors

Status: Completed

## Original Request
Explain and fix why both theme choices showed the same swatch colors when Aurora Pulse was selected.

## Thought Process
The theme option preview used live global colors for Midnight. Those globals change to the selected theme, so Midnight preview inherited Aurora colors after Aurora was selected.

## Implementation Plan
- Use `colorsFor(choice)` for each option preview.
- Keep current-theme colors for active UI border/background.
- Verify Android compile and diff check.

## Subtasks
| Status | Item |
| --- | --- |
| Completed | Fix per-option swatch colors |
| Completed | Verify Android compile |
| Completed | Update memory docs |

## Progress Log
- 2026-04-29: `ThemeOption` now builds swatches from `colorsFor(choice)` instead of live `SrPrimary/SrPlum/SrCoral`, so Midnight and Aurora previews stay visually distinct regardless of selected theme. Verified `./gradlew :app:compileDebugKotlin`.
