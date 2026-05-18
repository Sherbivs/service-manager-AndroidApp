# Service Manager — Agent Context Prompt

## CURRENT FOCUS
Routing/instruction role-specific standardization is complete. Current focus is packaging and release-hardening follow-through.

## RECENT CHANGES
- Rewrote template-section text by folder role (`Tips`, `Next Steps`, `Troubleshooting`) across all routers and instruction contracts.
- Added standardized `Tips`, `Next Steps`, and `Troubleshooting` sections to all router and instruction anchor files.
- Expanded network settings persistence and UI to support scheme/host/port + connect/read timeout controls.
- Updated network security baseline hosts and removed stale endpoint references in routers/docs.
- Fixed and verified targeted unit tests for settings and logs view models.
- Completed Android log-system scalability pass: archive request cancellation, dialog collector cleanup, debounced filtering, project filter, and archive pagination.
- Completed UX polish pass: services empty state, clearer error copy, haptic service actions, improved stopped-state visibility, larger log text, and selectable system error text.
- Verified all recent Android changes compile via `assembleDebug`.
- Updated documentation artifacts for completed QA/UX/scalability scope.

## PATCHSET HISTORY
| Date | Task ID | Summary |
| :--- | :--- | :--- |
| 2026-04-24 | SMA.014 | Modernization COMPLETE. 100% Green. 0 Redundancies. |
| 2026-04-28 | SMA.015 | Android UX/UI + scalability QA COMPLETE (logs and services hardening). |
| 2026-04-28 | SMA.016 | Documentation sync COMPLETE (Prompt/Tasklist/NEXT + Bible docs updates). |
| 2026-05-17 | SMA.017 | Network settings expansion COMPLETE (scheme/host/port + timeouts), stale router/docs endpoint purge, and targeted test stabilization. |
| 2026-05-17 | SMA.018 | Router/instruction section standardization COMPLETE (`Tips`, `Next Steps`, `Troubleshooting` on all anchors). |
| 2026-05-17 | SMA.019 | Role-specific router/instruction template rewrite COMPLETE (fixed section shape retained, folder-specific guidance applied). |
