# ROUTER
Title: Service Manager AndroidApp ??? Documentation Library
Purpose: Bible documentation and audit archive system for the Android app.
Owned Globs: docs/**
Last Updated (UTC): 2026-05-17T00:00:00Z

Areas:
- architecture-bible/   ??? System design, MVVM architecture, API integration patterns
- operations-bible/     ??? Build, sign, release, keystore setup, Play Store deployment
- development-bible/    ??? Local setup, coding conventions, contributing guide
	- 05-product-readiness-roadmap.md ??? Productization direction after UX/scalability QA
- archive/              ??? Audit and QA archive system with lifecycle stages
	- ROUTER.md           ??? Archive router and rules
	- tasks/ROUTER.md     ??? Parent router for task/audit lifecycle directories
	- tasks/README.md     ??? Lifecycle workflow and naming/index policy
	- tasks/new/ROUTER.md ??? Newly created audits not yet started
	- tasks/in-progress/ROUTER.md ??? Active audits under remediation
	- tasks/closed/ROUTER.md ??? Completed/substantially complete audits
	- tasks/canceled/ROUTER.md ??? Superseded/obsolete audits

Notes:
- All persistent knowledge belongs here. Transient notes go in Tasklist.md / Prompt.md.
- Each sub-bible has an INDEX.md listing all documents in that section.
- Canonical LAN endpoints for audit context: Service Manager API http://sensaimanager.drip:3500 (LAN IP: 192.168.23.106), TCB Shopify dev http://tcb.drip, and BLT Shopify dev http://blt.drip.
## Tips
- Keep architecture, operations, and development guidance in their correct bible domains.
- Promote only validated procedures into canonical docs.
- Keep transient planning notes out of bible docs and in task metadata files.

## Next Steps
1. Route each new document to the correct bible section.
2. Link new or changed pages from the relevant index.
3. Sync router entries whenever documentation topology changes.

## Troubleshooting
- If guidance is duplicated across sections, consolidate and leave one canonical location.
- If procedure text fails in runtime, replace it with evidence-backed instructions.
- If links break after moves, update parent and child router references immediately.