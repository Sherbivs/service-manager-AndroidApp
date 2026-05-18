# ROUTER
Title: Task Archive
Purpose: Historical record of completed audits, QA reports, and review artifacts organized by lifecycle status.
Owned Globs: docs/archive/**
Last Updated (UTC): 2026-05-17T00:00:00Z

Notes:
- Reports are organized into four lifecycle stages: new -> in-progress -> closed (or canceled).
- Use this archive for Android app audits and verification artifacts.
- Preserve full task/audit context so decisions and remediations are traceable.
- Environment baseline for archived audits: Service Manager API http://sensaimanager.drip:3500 (LAN IP: 192.168.23.106), TCB Shopify dev http://tcb.drip, and BLT Shopify dev http://blt.drip.

Areas:
- tasks/ROUTER.md ??? Parent router for archived audits and lifecycle subdirectories.
- tasks/README.md ??? Archive index, lifecycle workflow, and archival policy.
- tasks/new/ROUTER.md ??? New audit reports not yet started.
- tasks/in-progress/ROUTER.md ??? Reports actively undergoing remediation.
- tasks/closed/ROUTER.md ??? Completed or substantially complete reports.
- tasks/canceled/ROUTER.md ??? Obsolete or superseded reports.
## Tips
- Keep this router scoped to its owned paths and concrete file responsibilities.
- Use concise procedural guidance tied to actual failure modes in this folder.
- Synchronize this router whenever files are added, moved, renamed, or repurposed.

## Next Steps
1. Confirm task scope includes this folder before edits.
2. Apply minimal changes and verify behavior relevant to this scope.
3. Update router notes and upstream metadata after successful validation.

## Troubleshooting
- If scope ownership is unclear, resolve through nearest parent router and ops map.
- If file purpose changed but router did not, update both immediately.
- If verification is missing, keep task in-progress until evidence is captured.