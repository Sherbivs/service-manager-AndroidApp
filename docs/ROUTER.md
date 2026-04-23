# ROUTER
Title: Service Manager AndroidApp — Documentation Library
Purpose: Bible documentation and audit archive system for the Android app.
Owned Globs: docs/**
Last Updated (UTC): 2026-04-23T00:00:00Z

Areas:
- architecture-bible/   — System design, MVVM architecture, API integration patterns
- operations-bible/     — Build, sign, release, keystore setup, Play Store deployment
- development-bible/    — Local setup, coding conventions, contributing guide
- archive/              — Audit and QA archive system with lifecycle stages
	- ROUTER.md           — Archive router and rules
	- tasks/ROUTER.md     — Parent router for task/audit lifecycle directories
	- tasks/README.md     — Lifecycle workflow and naming/index policy
	- tasks/new/ROUTER.md — Newly created audits not yet started
	- tasks/in-progress/ROUTER.md — Active audits under remediation
	- tasks/closed/ROUTER.md — Completed/substantially complete audits
	- tasks/canceled/ROUTER.md — Superseded/obsolete audits

Notes:
- All persistent knowledge belongs here. Transient notes go in Tasklist.md / Prompt.md.
- Each sub-bible has an INDEX.md listing all documents in that section.
- Canonical LAN endpoints for audit context: Service Manager API http://192.168.23.83:3500 and Shopify dev service http://192.168.23.83:9292.
