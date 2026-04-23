# ROUTER
Title: Onboarding UI
Purpose: First-run screen prompting the user to enter the Service Manager server URL before reaching the main app.
Owned Globs: app/src/main/java/com/servicemanager/app/ui/onboarding/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Areas:
- OnboardingFragment.kt: URL entry on first launch; uses SettingsViewModel; navigates to servicesFragment on success

Integration Points:
- SettingsViewModel (ui/settings/) — shared ViewModel for URL save + test connection
- nav_graph.xml — onboardingFragment is the start destination; action_onboarding_to_services pops inclusive
- MainActivity.kt — hides BottomNavigationView when current destination is onboardingFragment
