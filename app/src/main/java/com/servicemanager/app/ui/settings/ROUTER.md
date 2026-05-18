# ROUTER
Title: Settings UI
Purpose: Network settings UI shared with onboarding support; allows persistent configuration of server scheme/host/port and request timeouts.
Owned Globs: app/src/main/java/com/servicemanager/app/ui/settings/**
Last Updated (UTC): 2026-05-17T00:00:00Z

Areas:
- SettingsFragment.kt: validates scheme/host/port/timeouts, composes URL preview, save/test/ping actions
- SettingsViewModel.kt: persists endpoint + timeout settings, tests connectivity, runs ping diagnostics

Integration Points:
- SecurePrefsHelper (util/) ??? persists endpoint fields (scheme/host/port), compatibility URL, and timeout settings
- ServiceRepository (data/repository/) ??? pings /api/system for test connection
- nav_graph.xml ??? settingsFragment destination (bottom nav)
## Tips
- Keep feature routers focused on UiState, ViewModel, Fragment, and integration points.
- Collect state with lifecycle-aware patterns to prevent background drift.
- Document feature-specific tests alongside behavior changes.

## Next Steps
1. Define state transitions before wiring UI events.
2. Implement ViewModel-driven updates and keep fragment logic thin.
3. Run targeted unit and UI tests and capture outcomes.

## Troubleshooting
- If UI does not update, inspect state emission path before view rendering code.
- If rotation causes regressions, verify state ownership remains in ViewModel.
- If actions duplicate, check lifecycle collection boundaries and click guards.