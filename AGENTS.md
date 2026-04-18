# Service Manager — Agent Operations Guide

## Mission
A lightweight agent system that routes tasks to capable sub-agents, never gets stuck, and maintains consistent project state. Safety, observability, and progress over perfection.

**Cross-References**:
- See `Patch.md` for contract rules, Absolutes, workflow (MUST READ FIRST)
- See `ops/NEXT.yaml` for task queue (up to 3 main + 1 quick task)
- See `Tasklist.md` for READY queue, dependencies, acceptance criteria
- See `Prompt.md` for current state, PATCHSET echo, recent changes
- See `README.md` for getting started guide

## Repo Map
- `server.js` — Express API server + process registry + health checks + auto-restart
- `services.json` — Service configuration (port, service definitions)
- `public/` — Dashboard SPA (HTML/CSS/JS, no build step)
- `ops/` — Governance surface (NEXT pointer, ROUTER.yaml, TOUCHMAP)
- `docs/` — Bible documentation (architecture, operations, development)
- `.github/` — AI instructions for Copilot/GPT agents

## Orientation Flow
1. **START HERE:** Read `Patch.md` completely before ANY work.
2. Review `ops/NEXT.yaml` to see active task queue.
3. Review `Prompt.md` for current state and recent changes.
4. Use `masterroutetable.md` alongside `ops/ROUTER.yaml` to navigate the project.
5. Consult `README.md` and `docs/` for architecture and operations context.
6. `Tasklist.md` captures backlog intent; only act after verifying readiness.

## How to Run Locally
- **Install:** `npm install`
- **Start:** `npm start`
- **Dashboard:** `http://localhost:3500` (or your LAN IP)

## Architecture Overview

```
Browser (Dashboard SPA)
  ↕ fetch /api/*
Express Server (server.js)
  ↕ spawn / taskkill
Managed Service Processes
```

### Key Components
- **Process Registry** — In-memory `Map<id, entry>` tracking spawned processes
- **Health Monitor** — HTTP polling to determine service liveness
- **Auto-Restart Engine** — Watches process exits, restarts with configurable delay
- **REST API** — CRUD operations on service lifecycle
- **Static Server** — Serves dashboard from `public/`

### API Quick Reference
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/services` | GET | List all services with live status |
| `/api/services/:id/start` | POST | Start a service |
| `/api/services/:id/stop` | POST | Stop a service |
| `/api/services/:id/restart` | POST | Restart a service |
| `/api/system` | GET | System info (hostname, IP, memory, uptime) |
| `/api/logs?lines=50` | GET | Recent log lines |

## Capabilities & Routing Hints
- **planner:** Break down goals into measurable tasks. Use Patch.md workflow.
- **researcher:** Search, synthesize, attach citations.
- **implementer:** Write/change code and config; run tests. MUST follow Patch.md workflow.
- **reviewer:** Verify diffs, run safety checklists; approve or request changes.

## Contract Workflow Quickstart
1. Open `Patch.md` and review the task queue system.
2. Check `ops/NEXT.yaml` for available task slots.
3. Plan file operations following Patch.md guidelines.
4. Execute edits. Maintain formatting, respect existing code style.
5. Update `Prompt.md` and `ops/NEXT.yaml` before finishing.
6. Leave the repository in a consistent state (routers valid, metadata synced).

## Router Contract (Mandatory)
- Every change MUST be accompanied by routing updates when adding files, renaming directories, or changing file purposes.
- Update: affected `ROUTER.md` (parent and child), `masterroutetable.md`, and `ops/ROUTER.yaml` as needed.
- Do not consider a task complete until routers reflect the changes.

## Safety Invariants
- Never exfiltrate secrets in outputs.
- Never overwrite user `services.json` entries without explicit permission.
- On error, self-forgive: reset working memory, keep the log, try a smaller step.
- Keep every change reversible and minimal.

## Stuckness Detection & Recovery
A run is **stuck** if:
- No new artifact or state change in 3 consecutive steps
- Same error seen ≥2 times in 10 minutes
- Budget exceeded without milestone progress

Recovery (in order):
1. Shrink the step (narrow scope)
2. Swap strategy (different approach)
3. Reset context (clear ephemeral state)
4. Escalate (document blocker, ask human)

## Documentation Standards
- All knowledge flows into the Bible system:
  - `docs/architecture-bible/` — System design, components, API
  - `docs/operations-bible/` — Install, configure, monitor, troubleshoot
  - `docs/development-bible/` — Setup, conventions, contributing
- Transient notes go in `Tasklist.md` or `Prompt.md`, NOT standalone docs.
- Every directory has a `ROUTER.md`.

## Common Pitfalls
1. **Don't add dependencies** — Express only unless strongly justified.
2. **Router updates** — Forgetting breaks AI navigation.
3. **services.json** — User configuration, never overwrite without permission.
4. **Windows-specific** — `taskkill` is Windows-only.
5. **No TypeScript** — Vanilla JS, no build step.

## Checklist Before Exit
- `Prompt.md` updated with accurate summary and NEXT pointer status.
- `ops/NEXT.yaml` task queue reflects current work state.
- `Tasklist.md` updated with task status changes.
- Routers synchronized if structural changes made.
- Commit message references task ID and change summary.

## Done Criteria (Per Task)
- Changes implemented and verified.
- Knowledge updated (`docs/` + this guide if behavior changed).
- Routers updated if structural changes.
- Prompt.md PATCHSET echo current.

---

**Document Version:** 1.0 (2026-04-18) — Initial bootstrap
**Last Updated:** 2026-04-18T00:00:00Z
