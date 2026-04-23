# ROUTER — App Module
Title: Service Manager AndroidApp — App Module
Purpose: Main application module containing UI, DI, and Data layers.
Owned Globs: app/src/*
Last Updated (UTC): 2026-04-23T00:00:00Z

## Areas
- src/main/java/com/servicemanager/app/ui/      — ViewModels and Fragments (MVVM)
- src/main/java/com/servicemanager/app/ui/onboarding/ — Onboarding Flow
- src/main/java/com/servicemanager/app/ui/services/ — Services List and Actions
- src/main/java/com/servicemanager/app/ui/system/   — System Info
- src/main/java/com/servicemanager/app/ui/logs/     — Log Viewers
- src/main/java/com/servicemanager/app/ui/settings/ — Server Settings
- src/main/java/com/servicemanager/app/di/      — Hilt Modules
- src/main/java/com/servicemanager/app/data/    — Repositories, DTOs, and API interfaces
- src/main/java/com/servicemanager/app/util/    — Utilities (EncryptedPrefsHelper, etc.)
- src/main/res/layout/                          — ViewBinding XML layouts
- src/main/res/navigation/                      — NavGraph (Navigation Component)
- src/main/res/menu/                            — BottomNavigationView menus
- src/main/res/xml/                             — Network Security Config

## Key Files
- MainActivity.kt        — Single activity hosting the NavHostFragment
- ServiceManagerApp.kt   — Hilt Application class
- nav_graph.xml          — Defines app navigation flow
