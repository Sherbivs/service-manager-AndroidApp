# ROUTER
Title: Closed Audit Reports
Purpose: Completed or substantially complete audit reports with verified remediation outcomes.
Owned Globs: docs/archive/tasks/closed/**
Last Updated (UTC): 2026-04-24T00:00:00Z

Notes:
- Store completed audits and QA artifacts here.
- Keep report metadata and conclusions intact for traceability.

Areas:
- C02-QA-AUDIT-002.md: Android Studio Problems Export Remediation Cycle ??? CLOSED 2026-04-24 (Waves 0???4, 5/5 QA gates GREEN)
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