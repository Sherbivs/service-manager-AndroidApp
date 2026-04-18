# § 1 — System Overview

**Service Manager** is a lightweight, zero-dependency-beyond-Express service orchestrator with a web dashboard for managing local development services.

## Technology Stack

| Layer | Technology | Notes |
|-------|-----------|-------|
| Runtime | Node.js | No build step, vanilla JS |
| Server | Express 4.x | Static file serving + JSON API |
| Frontend | HTML/CSS/JS | No framework, no bundler |
| Process Mgmt | child_process (spawn) | Native Node.js process control |
| Config | JSON file | `services.json` — hot-reloaded per request |

## Project Structure

```
service-manager/
├── server.js          # Express server + process registry + API
├── services.json      # Service definitions and port config
├── package.json       # Dependencies (express only)
├── public/            # Dashboard SPA
│   ├── index.html     # Dashboard shell
│   ├── app.js         # Client logic
│   └── style.css      # Dark theme styles
├── ops/               # Governance (NEXT, ROUTER, TOUCHMAP)
├── docs/              # Bible documentation system
├── .github/           # AI instructions
├── Patch.md           # AI Project Manager Contract
├── Prompt.md          # Current state for automation
├── Tasklist.md        # Task backlog
├── AGENTS.md          # Agent operations guide
└── masterroutetable.md # Text-first router map
```

## Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                    Browser (Dashboard)                │
│  ┌─────────┐  ┌─────────┐  ┌──────────────────────┐│
│  │index.html│  │ app.js  │  │     style.css        ││
│  └─────────┘  └────┬────┘  └──────────────────────┘│
│                     │ fetch /api/*                    │
└─────────────────────┼───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                  server.js (Express)                  │
│  ┌──────────┐  ┌──────────┐  ┌────────────────────┐│
│  │ Static   │  │ REST API │  │  Process Registry  ││
│  │ Serving  │  │ /api/*   │  │  (Map<id, entry>)  ││
│  └──────────┘  └──────────┘  └─────────┬──────────┘│
│                                         │ spawn()    │
└─────────────────────────────────────────┼───────────┘
                                          │
┌─────────────────────────────────────────▼───────────┐
│              Managed Service Processes                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │ Service A│  │ Service B│  │ Service C│  ...      │
│  └──────────┘  └──────────┘  └──────────┘          │
└─────────────────────────────────────────────────────┘
```

## Key Invariants

1. **Config is hot-reloaded** — `services.json` is read on every API request, no restart needed to add services.
2. **Stateless server** — The only runtime state is the in-memory process registry (`Map`). No database.
3. **Network accessible** — Binds to `0.0.0.0`, reachable from any device on the LAN.
4. **Auto-restart** — Services with `autoRestart: true` are restarted on crash with configurable delay.
5. **Health-aware** — Services with `healthCheck` URLs are polled to determine actual running status.
