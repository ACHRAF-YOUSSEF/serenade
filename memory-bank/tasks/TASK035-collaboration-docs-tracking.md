# TASK035 - Collaboration Docs Tracking

## Status
Completed on 2026-04-26

## Original Request
User noted that collaboration with a teammate needs the memory bank, project markdown files, `.claude`, and `.github` instructions.

## Thought Process
- `.gitignore` was hiding `.github/`, `.claude/`, `progress.md`, plans, and most markdown files.
- Shared collaboration docs should be versioned so teammates and agents share the same project state and operating rules.
- Local Claude settings may contain machine-specific preferences and should stay ignored.

## Implementation Plan
1. Unignore shared root markdown files.
2. Unignore `memory-bank/**/*.md`.
3. Unignore `.github/instructions/**/*.md`.
4. Unignore `.claude/commands/**/*.md` and `.claude/skills/**/*.md`.
5. Keep `.claude/settings.local.json` ignored.
6. Verify visible files and scan shared docs for obvious secret patterns.

## Subtasks
| Item | Status |
| --- | --- |
| `.gitignore` allow-list | Done |
| Local Claude settings remain ignored | Done |
| Collaboration docs visibility check | Done |
| Secret pattern scan | Done |

## Progress Log
- 2026-04-26: Updated `.gitignore` so shared markdown, memory-bank, `.github/instructions`, and `.claude` commands/skills are visible to git.
- 2026-04-26: Kept `.claude/settings.local.json` ignored.
- 2026-04-26: Checked doc status and scanned shared docs for obvious secret patterns; matches were placeholders/terminology, not committed secret values.
