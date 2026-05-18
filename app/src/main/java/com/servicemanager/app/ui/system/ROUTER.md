# ROUTER
Title: System Info UI
Purpose: System info screen ??? displays live server metadata (hostname, IP, Node.js version, memory, uptime).
Owned Globs: app/src/main/java/com/servicemanager/app/ui/system/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Areas:
- SystemFragment.kt: SwipeRefreshLayout host; onStart() triggers auto-refresh on screen focus; lifecycle-aware StateFlow collection
- SystemViewModel.kt: UDF state holder; loadSystemInfo() fetches from repo; init + onStart both call loadSystemInfo()
- SystemUiState: sealed class inline in SystemViewModel.kt (Loading, Success, Error)

Integration Points:
- ServiceRepository (data/repository/) ??? getSystemInfo()
- SystemInfoDto / MemoryInfoDto (data/model/) ??? response DTOs
- nav_graph.xml ??? systemFragment destination (bottom nav)
- strings.xml ??? label_hostname, label_platform, label_ip, label_node_version, label_uptime, label_memory, value_unknown
- fragment_system.xml ??? textHostname, textPlatform, textIpAddress, textNodeVersion, textUptime, textMemory, textError, scrollContent
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