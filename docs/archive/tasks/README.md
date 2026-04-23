# Task Archive — Service Manager Android App

Location: docs/archive/tasks/
Last Updated: 2026-04-23

## Environment Baseline

- Service Manager API: http://192.168.23.83:3500
- Shopify Dev Service (TCB Party Rental): http://192.168.23.83:9292

Use these endpoints as the canonical baseline when creating and reviewing audits tied to the active LAN environment.

## Archive Lifecycle

Reports move through four stages:

| Stage | Directory | Description |
|-------|-----------|-------------|
| New | new/ | Freshly created audit reports, not yet started |
| In-Progress | in-progress/ | Actively undergoing remediation work |
| Closed | closed/ | Completed or substantially complete; remediation verified |
| Canceled | canceled/ | Obsolete, superseded, or no longer needed |

Workflow:
1. Create new audit report in new/.
2. Move to in-progress/ when remediation work begins.
3. Move to closed/ when substantially complete or fully remediated.
4. Move to canceled/ if the audit is invalidated or superseded.
5. Update ROUTER.md files (parent + source + destination) and this README on every move.

## Archive Policy

- Trigger: archive completed artifacts as needed to keep active planning files focused.
- Exception: QA reports and review artifacts may be archived immediately.
- Structure: keep reports scoped and readable; split oversized artifacts when needed.
- Preserve: findings, evidence, remediation state, and outcome notes.

## Naming Convention

Use cycle-prefixed file names to group related audits:

C{NN}-{TYPE}-{ID}.md

Examples:
- C01-QA-AUDIT-001.md
- C01-SEC-AUDIT-001.md
- C02-PERF-AUDIT-001.md

## Archive Index

### New

- [C01-QA-AUDIT-001.md](new/C01-QA-AUDIT-001.md) — Core Foundation & Stabilization Audit (2026-04-23)

### In-Progress

*(no active reports)*

### Closed

- [C02-QA-AUDIT-002.md](closed/C02-QA-AUDIT-002.md) — Android Studio Problems Export Remediation Cycle (2026-04-23; closed 2026-04-24)

None.

### Canceled

None.