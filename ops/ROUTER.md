# ROUTER
Title: Service Manager AndroidApp ??? Ops & Governance
Purpose: Governance surface for the Android app project. Contains task queue, routing map, and change tracking metadata.
Owned Globs: ops/*
Last Updated (UTC): 2026-04-22T00:00:00Z

Notes:
- The ops/ directory mirrors the pattern from the service-manager Node.js repo.
- AI agents MUST update NEXT.yaml task statuses before completing any task.
- ROUTER.yaml is the machine-readable canonical routing map for the whole project.

Files:
- NEXT.yaml      ??? Active task queue pointer (up to 3 main + 1 quick task)
- ROUTER.yaml    ??? Canonical file/directory routing map (full project)
- TOUCHMAP.yaml  ??? File touch frequency tracker (update after each commit)
- ROUTER.md      ??? This file (human-readable ops directory documentation)
## Tips
- Treat ops files as the control plane for task state, routing truth, and scope tracking.
- Keep NEXT pointer, task status, and touch map synchronized as a single operation.
- Record blockers explicitly so queue state remains deterministic for the next agent.

## Next Steps
1. Confirm the active task is READY and dependencies are satisfied.
2. Perform scoped work and capture validation evidence for pass or fail.
3. Update NEXT, Tasklist, and Prompt before closing the task.

## Troubleshooting
- If NEXT and Tasklist disagree, reconcile them before any additional implementation.
- If touched files exceed declared scope, update touch metadata and rationale.
- If no READY task exists, promote eligible work using documented preflight policy.