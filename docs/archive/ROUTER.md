# ROUTER
Title: Task Archive
Purpose: Historical record of completed audits, QA reports, and review artifacts organized by lifecycle status.
Owned Globs: docs/archive/**
Last Updated (UTC): 2026-04-23T00:00:00Z

Notes:
- Reports are organized into four lifecycle stages: new -> in-progress -> closed (or canceled).
- Use this archive for Android app audits and verification artifacts.
- Preserve full task/audit context so decisions and remediations are traceable.
- Environment baseline for archived audits: Service Manager API http://192.168.23.83:3500 and Shopify dev service http://192.168.23.83:9292.

Areas:
- tasks/ROUTER.md — Parent router for archived audits and lifecycle subdirectories.
- tasks/README.md — Archive index, lifecycle workflow, and archival policy.
- tasks/new/ROUTER.md — New audit reports not yet started.
- tasks/in-progress/ROUTER.md — Reports actively undergoing remediation.
- tasks/closed/ROUTER.md — Completed or substantially complete reports.
- tasks/canceled/ROUTER.md — Obsolete or superseded reports.