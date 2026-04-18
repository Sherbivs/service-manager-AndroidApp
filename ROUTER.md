# ROUTER
Title: Service Manager — Root
Purpose: Elastic service management dashboard — manage, monitor, and control local dev services from a single web UI.
Owned Globs: *
Last Updated (UTC): 2026-04-18T00:00:00Z

Notes:
- Root meta-router for Service Manager. All sub-routers branch from here.
- Project uses vanilla Node.js + Express with no build step.
- Dashboard is a static SPA served from public/.

Areas:
- server.js — Express API server, process registry, health checks, auto-restart engine
- services.json — Service configuration (port, service definitions)
- package.json — Dependencies and npm scripts
- AGENTS.md — Agent operations guide
- Patch.md — AI Project Manager Contract
- Prompt.md — Current state, PATCHSET echo
- Tasklist.md — Task backlog with READY queue
- masterroutetable.md — Text-first router map
- public/ — Dashboard SPA (HTML/CSS/JS)
- ops/ — Governance surface (NEXT pointer, tasklist, routing metadata)
- docs/ — Documentation (architecture-bible, operations-bible, development-bible)
- .github/ — AI instructions for Copilot/GPT agents

Subrouters:
- public/ROUTER.md — Dashboard frontend
- ops/ROUTER.md — Ops & Governance
- docs/ROUTER.md — Documentation library
- .github/ROUTER.md — AI agent configuration
