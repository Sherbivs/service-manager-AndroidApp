# ROUTER
Title: System Info UI
Purpose: System info screen — displays live server metadata (hostname, IP, Node.js version, memory, uptime).
Owned Globs: app/src/main/java/com/servicemanager/app/ui/system/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Areas:
- SystemFragment.kt: SwipeRefreshLayout host; onStart() triggers auto-refresh on screen focus; lifecycle-aware StateFlow collection
- SystemViewModel.kt: UDF state holder; loadSystemInfo() fetches from repo; init + onStart both call loadSystemInfo()
- SystemUiState: sealed class inline in SystemViewModel.kt (Loading, Success, Error)

Integration Points:
- ServiceRepository (data/repository/) — getSystemInfo()
- SystemInfoDto / MemoryInfoDto (data/model/) — response DTOs
- nav_graph.xml — systemFragment destination (bottom nav)
- strings.xml — label_hostname, label_platform, label_ip, label_node_version, label_uptime, label_memory, value_unknown
- fragment_system.xml — textHostname, textPlatform, textIpAddress, textNodeVersion, textUptime, textMemory, textError, scrollContent
