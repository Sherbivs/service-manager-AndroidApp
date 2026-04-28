# ROUTER
Title: Settings UI
Purpose: Server URL settings screen and onboarding — allows user to configure and test the Service Manager server URL.
Owned Globs: app/src/main/java/com/servicemanager/app/ui/settings/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Areas:
- SettingsFragment.kt: URL input, validate, save, test connection; shared by OnboardingFragment
- SettingsViewModel.kt: saveServerUrl (encrypts to prefs), testConnection (pings /api/system)

Integration Points:
- SecurePrefsHelper (util/) — persists server URL
- ServiceRepository (data/repository/) — pings /api/system for test connection
- nav_graph.xml — settingsFragment destination (bottom nav)
