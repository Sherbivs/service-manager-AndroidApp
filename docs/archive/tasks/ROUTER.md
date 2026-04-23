# ROUTER
Title: Archive Records
Purpose: Audit reports, QA reports, and review artifacts organized by lifecycle status.
Owned Globs: docs/archive/tasks/**
Last Updated (UTC): 2026-04-23T02:00:00Z

Notes:
- Reports are organized into four lifecycle stages: new -> in-progress -> closed (or canceled).
- New audits land in new/, move to in-progress/ during remediation, and to closed/ when complete.
- Reports that are obsolete, superseded, or no longer needed go to canceled/.
- Update this router, sub-routers, and the archive README index whenever files move stages.

Areas:
- README.md — Archive index, lifecycle workflow, and archival policy documentation.
- new/ — Audit reports not yet started (new/ROUTER.md): C01-QA-AUDIT-001.md.
- in-progress/ — Audit reports actively undergoing remediation (in-progress/ROUTER.md): C02-QA-AUDIT-002.md.
- closed/ — Completed or substantially complete audit reports (closed/ROUTER.md).
- canceled/ — Obsolete or superseded audit reports (canceled/ROUTER.md).