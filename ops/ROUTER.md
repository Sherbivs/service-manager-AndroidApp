# ROUTER
Title: Service Manager AndroidApp — Ops & Governance
Purpose: Governance surface for the Android app project. Contains task queue, routing map, and change tracking metadata.
Owned Globs: ops/*
Last Updated (UTC): 2026-04-22T00:00:00Z

Notes:
- The ops/ directory mirrors the pattern from the service-manager Node.js repo.
- AI agents MUST update NEXT.yaml task statuses before completing any task.
- ROUTER.yaml is the machine-readable canonical routing map for the whole project.

Files:
- NEXT.yaml      — Active task queue pointer (up to 3 main + 1 quick task)
- ROUTER.yaml    — Canonical file/directory routing map (full project)
- TOUCHMAP.yaml  — File touch frequency tracker (update after each commit)
- ROUTER.md      — This file (human-readable ops directory documentation)
