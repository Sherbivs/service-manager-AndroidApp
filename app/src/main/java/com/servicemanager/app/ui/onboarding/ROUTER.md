# ROUTER
Title: Onboarding UI
Purpose: First-run screen prompting the user to enter and verify Service Manager connectivity before reaching the main app.
Owned Globs: app/src/main/java/com/servicemanager/app/ui/onboarding/**
Last Updated (UTC): 2026-05-17T00:00:00Z

Areas:
- OnboardingFragment.kt: endpoint URL entry on first launch; uses SettingsViewModel for connection test and settings save; navigates to servicesFragment on success

Integration Points:
- SettingsViewModel (ui/settings/) ??? shared ViewModel for network settings save + connection test
- nav_graph.xml ??? onboardingFragment is the start destination; action_onboarding_to_services pops inclusive
- MainActivity.kt ??? hides BottomNavigationView when current destination is onboardingFragment
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