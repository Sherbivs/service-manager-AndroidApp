# ROUTER ??? App Module
Title: Service Manager AndroidApp ??? App Module
Purpose: Main application module containing UI, DI, and Data layers.
Owned Globs: app/src/*
Last Updated (UTC): 2026-05-17T00:00:00Z

## Areas
- src/main/java/com/servicemanager/app/ui/      ??? ViewModels and Fragments (MVVM)
- src/main/java/com/servicemanager/app/ui/onboarding/ ??? Onboarding Flow
- src/main/java/com/servicemanager/app/ui/services/ ??? Services List and Actions
- src/main/java/com/servicemanager/app/ui/system/   ??? System Info
- src/main/java/com/servicemanager/app/ui/logs/     ??? Log Viewers
- src/main/java/com/servicemanager/app/ui/settings/ ??? Network Settings (scheme/host/port, timeouts, connectivity tests)
- src/main/java/com/servicemanager/app/di/      ??? Hilt Modules
- src/main/java/com/servicemanager/app/data/    ??? Repositories, DTOs, and API interfaces
- src/main/java/com/servicemanager/app/util/    ??? Utilities (SecurePrefsHelper, etc.)
- src/main/res/layout/                          ??? ViewBinding XML layouts
- src/main/res/navigation/                      ??? NavGraph (Navigation Component)
- src/main/res/menu/                            ??? BottomNavigationView menus
- src/main/res/xml/                             ??? Network Security Config

## Key Files
- MainActivity.kt        ??? Single activity hosting the NavHostFragment
- ServiceManagerApp.kt   ??? Hilt Application class
- nav_graph.xml          ??? Defines app navigation flow
## Tips
- Keep module-level guidance aligned with MVVM, UDF, and Hilt boundaries.
- Route network configuration through secure settings, not hardcoded values.
- Maintain clear mapping between app module structure and navigation ownership.

## Next Steps
1. Confirm feature changes preserve single-activity navigation architecture.
2. Validate build, lint, and tests after module-level updates.
3. Update router details when package layout or module responsibilities change.

## Troubleshooting
- If dependency injection fails, verify annotations and module bindings first.
- If navigation breaks, validate graph destinations and argument contracts.
- If build variants diverge, reconcile release and debug configuration assumptions.