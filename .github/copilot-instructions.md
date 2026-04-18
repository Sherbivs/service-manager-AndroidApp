# Copilot Instructions — Service Manager

## Project Overview

Service Manager is an elastic service management dashboard — a lightweight Node.js/Express server with a vanilla web dashboard for managing, monitoring, and controlling local dev services from a single UI.

**Stack:** Node.js, Express, vanilla HTML/CSS/JS. No build step, single dependency.

## Critical: Start Here Before Any Work

### Orientation Flow (Required Reading)
1. **`Patch.md`** — AI Project Manager Contract. Defines Absolutes, workflow, and task discipline.
2. **`Prompt.md`** — Current timestamp, recent changes, PATCHSET echo template.
3. **`Tasklist.md`** — Active backlog with READY queue, dependencies, acceptance criteria.
4. **`ops/NEXT.yaml`** — Current task pointer (must point to READY task).
5. **`AGENTS.md`** — Comprehensive operations guide.

### Router Navigation System
- **`ops/ROUTER.yaml`** — Canonical AI routing map. Start here to locate any file.
- **`masterroutetable.md`** — Human-readable router index.
- **Every directory has `ROUTER.md`** documenting purpose, owned files, and key integration points.
- When touching code: update parent + child routers in same commit.

### Core Control Documents (Never Rename/Delete)
- `Patch.md`, `Tasklist.md`, `Prompt.md` — Meta workflow coordination
- `ops/ROUTER.yaml`, `masterroutetable.md` — Routing manifests
- Any `ROUTER.md` file — Subsystem documentation

## Architecture Essentials

### Project Structure
```
server.js        — Express API server + process registry + health checks + auto-restart
services.json    — Service configuration (port, service definitions)
public/          — Dashboard SPA (index.html, app.js, style.css)
ops/             — Governance (NEXT.yaml, ROUTER.yaml, TOUCHMAP.yaml)
docs/            — Bible documentation (architecture, operations, development)
```

### Key Design Principles
- **Zero unnecessary dependencies** — Express only. Don't add packages without strong justification.
- **No build step** — Vanilla JS throughout. Files served directly.
- **Hot-reload config** — `services.json` re-read on every API request.
- **LAN accessible** — Binds to `0.0.0.0`.
- **Windows-first** — Process management uses `taskkill`. Cross-platform is a future goal.

### API Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/services` | GET | List all services with live status |
| `/api/services/:id/start` | POST | Start a service |
| `/api/services/:id/stop` | POST | Stop a service |
| `/api/services/:id/restart` | POST | Restart a service |
| `/api/system` | GET | System info |
| `/api/logs?lines=50` | GET | Recent log lines |

## Development Workflows

### Common Commands
```bash
npm install    # Install dependencies
npm start      # Start the server
```

### Router Discipline
Every code change MUST update routers in same commit:
1. Update parent `ROUTER.md` (e.g., `public/ROUTER.md`)
2. Create child `ROUTER.md` if creating new subdirectory
3. Update `masterroutetable.md` if adding directories

### Commit Messages
```
[Task SM.XXX.YYY] Brief summary

Why: Business/technical reason
Changes: Major modifications
Testing: How verified
```

## Documentation Standards

### Bible System
All knowledge flows into the Bible docs:
- **Architecture Bible** (`docs/architecture-bible/`) — System design, components, API
- **Operations Bible** (`docs/operations-bible/`) — Install, configure, monitor, troubleshoot
- **Development Bible** (`docs/development-bible/`) — Setup, conventions, contributing

Transient notes belong in `Tasklist.md` or `Prompt.md`, NOT as standalone markdown files.

## Common Pitfalls

1. **Don't add dependencies** unless absolutely necessary. This is a lightweight tool.
2. **Router updates** — Forgetting to update `ROUTER.md` files breaks navigation for AI agents.
3. **services.json** — Never overwrite user service entries without explicit permission.
4. **Windows-specific** — `taskkill` is Windows-only. Guard cross-platform changes.
5. **No TypeScript** — Keep it vanilla JS. The zero-build-step design is intentional.

## Quick Reference

- **Architecture:** `docs/architecture-bible/INDEX.md`
- **Operations:** `docs/operations-bible/INDEX.md`
- **Development:** `docs/development-bible/INDEX.md`
- **Task backlog:** `Tasklist.md`
- **Current task:** `ops/NEXT.yaml`
