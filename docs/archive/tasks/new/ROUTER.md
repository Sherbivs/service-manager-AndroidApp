# ROUTER
Title: New Audit Reports
Purpose: Holding area for newly created audit reports before remediation begins.
Owned Globs: docs/archive/tasks/new/**
Last Updated (UTC): 2026-04-23T02:00:00Z

Notes:
- Place new audit artifacts here first.
- Move to in-progress/ once implementation work starts.

Areas:
- C01-QA-AUDIT-001.md: Core Foundation & Stabilization Audit
## Tips
- Use archive stages to track audit lifecycle from new to in-progress to closed or canceled.
- Keep audit artifacts immutable except for status transitions with evidence updates.
- Ensure each report includes scope, findings, remediation, and verification outcome.

## Next Steps
1. Place new audits in the new stage with required naming conventions.
2. Move to in-progress when remediation starts and update stage routers.
3. Close or cancel with verification results and index synchronization.

## Troubleshooting
- If stage and index disagree, fix routing and index files in the same commit.
- If evidence is missing, keep report in-progress and capture validation first.
- If audit scope changed, create a superseding report and cross-link both entries.