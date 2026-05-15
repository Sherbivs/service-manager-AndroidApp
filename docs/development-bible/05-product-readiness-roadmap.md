# Product Readiness Roadmap
**Project:** Service Manager Android App
**Status:** Active
**Last Updated:** 2026-04-28

## Purpose
Define practical productization direction after the Android UX/UI and scalability QA cycle, focusing on a sellable, supportable mobile companion for Service Manager.

## Completed Foundation (2026-04-28)
- Log archive scalability hardening:
  - request cancellation for in-flight archive searches
  - dialog collector lifecycle cleanup
  - archive paging controls (prev/next + page info)
  - project filter support for global archive search
- Visibility and usability improvements:
  - services empty state
  - clearer network/server/timeout error copy
  - haptic feedback on core service actions
  - explicit stopped-state service label in health row
  - larger log text for readability
  - selectable error text in system screen
- Verification:
  - assembleDebug successful

## Product Expectations (Near-Term)
1. Fast confidence loop:
- Users should confirm service state quickly without deciphering hidden statuses.

2. Operational clarity:
- Errors should guide recovery actions, not only report failure.

3. Scalable log workflow:
- Archive browsing must remain usable as data volume and service count increase.

4. Trust signals:
- Stable interactions, predictable feedback, and clear status language are mandatory for internal customer rollout.

## Differentiation Targets
1. Mobile-first operations:
- Make common actions faster than desktop workflow for on-call usage.

2. Progressive complexity:
- Keep defaults simple for new operators while preserving deeper tooling for advanced users.

3. Team fit:
- Prepare UI and architecture for role-aware interaction patterns and shared operational ownership.

## Delivery Plan
## Now
- Keep polishing core workflows:
  - service actions
  - logs and archive search
  - system health clarity
- Maintain build health gates on every patch.

## Next
- Batch actions for services (multi-select start/stop/restart).
- Favorites or pinned services for high-frequency monitoring.
- Role-aware UI gating scaffold (viewer/operator/admin).

## Later
- Multi-server switcher for environments (dev/staging/prod).
- Incident timeline view with action history.
- Smart notifications with contextual service details.

## Notes
This roadmap is intentionally implementation-oriented and should be updated as backlog items are accepted into Tasklist.md with SMA task IDs.
