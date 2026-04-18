# ROUTER
Title: Ops & Governance
Purpose: Canonical operational control-plane — NEXT pointer, routing metadata, tasklist, touchmap.
Owned Globs: ops/**
Last Updated (UTC): 2026-04-18T00:00:00Z

Notes:
- Modeled after SensaiOS governance surface, scaled for a small utility project.
- All AI agents read ops/NEXT.yaml first to know what to work on.

Key Files:
- ops/NEXT.yaml — Current task pointer and queue
- ops/ROUTER.yaml — Canonical path map for AI navigation
- ops/TOUCHMAP.yaml — File ownership per task

Areas:
- NEXT.yaml — Execution pointer (up to 3 main tasks + 1 quick task)
- ROUTER.yaml — AI routing map (categories, entrypoints, meta files)
- ROUTER.md — This file
- TOUCHMAP.yaml — File scope boundaries per task
