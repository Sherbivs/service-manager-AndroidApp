# ROUTER
Title: Logs UI
Purpose: Global log viewer with configurable line count, expand-on-tap per line, copy to clipboard, and per-service archive search.
Owned Globs: app/src/main/java/com/servicemanager/app/ui/logs/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Areas:
- LogsFragment.kt: Chip-based line count selector (50/100/200/500); RecyclerView auto-scrolls to bottom on load; copy FAB writes to ClipboardManager; archive search card with service ID + query inputs.
- LogsViewModel.kt: UDF state holder; loadLogs(lines) fetches global logs; searchArchive(serviceId, query) searches per-service archive. Two StateFlows: uiState + archiveState.
- LogsUiState / ArchiveUiState: Sealed classes (Loading/Success/Error; Idle/Loading/Success/Error).
- LogLineAdapter.kt: ListAdapter<String>; tapping a line toggles expand (maxLines=1 ??? Int.MAX_VALUE).

Integration Points:
- ServiceRepository (data/repository/) ??? getGlobalLogs(lines), searchArchiveLogs(serviceId, query)
- ApiService (data/api/) ??? GET /api/logs?lines=N, GET /api/services/{id}/logs/archive?q=query
- LogsResponseDto (data/model/) ??? lines: List<String>
- nav_graph.xml ??? logsFragment destination (bottom nav)
- strings.xml ??? logs_empty, hint_service_id, hint_archive_query, btn_search_archive, logs_copied, archive_no_results, content_description_copy_logs
- fragment_logs.xml ??? chipGroupLines, swipeRefresh, recyclerLogs, textError, cardArchiveSearch, editServiceId, editArchiveQuery, btnSearchArchive, textArchiveStatus, recyclerArchive, fabCopyLogs
- item_log_line.xml ??? textLogLine (monospace, maxLines=1 default)
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