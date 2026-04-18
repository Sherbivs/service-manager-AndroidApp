# Master Router Table
Last Updated (UTC): 2026-04-18T00:00:00Z
Purpose: Text-only reference of every router and the primary files/routes they cover.

---

## Top-Level Directory Overview

| Directory | Purpose | ROUTER.md |
|-----------|---------|-----------|
| `public/` | Dashboard SPA (HTML/CSS/JS) | ✅ |
| `ops/` | Governance & operations | ✅ |
| `docs/` | Documentation (Bible system) | ✅ |
| `.github/` | AI instructions | ✅ |

---

## Router-of-Routers

- `ROUTER.md` — Root meta-router
  - `public/ROUTER.md` — Dashboard frontend (index.html, app.js, style.css)
  - `ops/ROUTER.md` — Ops & Governance (NEXT.yaml, ROUTER.yaml, TOUCHMAP.yaml)
  - `docs/ROUTER.md` — Documentation library
    - `docs/architecture-bible/INDEX.md` — System architecture documentation
    - `docs/operations-bible/INDEX.md` — Operational procedures
    - `docs/development-bible/INDEX.md` — Development workflows
  - `.github/ROUTER.md` — AI agent configuration and instructions

## Key Files (Root)

| File | Purpose |
|------|---------|
| `server.js` | Express API server, process registry, health checks, auto-restart |
| `services.json` | Service configuration (port, service definitions) |
| `package.json` | Dependencies and npm scripts |
| `Patch.md` | AI Project Manager Contract |
| `Prompt.md` | Current state, PATCHSET echo, recent changes |
| `Tasklist.md` | Task backlog with READY queue |
| `AGENTS.md` | Agent operations guide |
| `masterroutetable.md` | This file — text-first router map |
