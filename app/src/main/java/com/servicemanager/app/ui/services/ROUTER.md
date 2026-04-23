# ROUTER
Title: Services UI
Purpose: Services list screen — displays all managed services with live status, health metrics, and start/stop/restart actions.
Owned Globs: app/src/main/java/com/servicemanager/app/ui/services/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Areas:
- ServicesFragment.kt: RecyclerView host, pull-to-refresh, polling lifecycle (onStart/onStop); observes pendingActions + actionSuccess + actionError
- ServicesViewModel.kt: UDF state holder; loadServices (forced), loadServicesIfStale (rotation-safe); 10s polling; triggerAction dispatches Start/Stop/Restart with pendingActions tracking, actionSuccess/actionError emission, retryLastAction()
- ServicesAdapter.kt: ListAdapter<ServiceDto>; status chip color-coding; health % + latency; buttons disabled when running/stopped or in-flight (updatePendingActions)
- ServicesUiState: sealed class inline in ServicesViewModel.kt (Loading, Success, Error)

Integration Points:
- ServiceRepository (data/repository/) — getServices(), startService(), stopService(), restartService()
- nav_graph.xml — servicesFragment destination (bottom nav start)
- strings.xml — format_project, format_health_percent, format_latency_ms, retry
- colors.xml — status_running_bg/text, status_stopped_bg/text, status_error_bg/text
