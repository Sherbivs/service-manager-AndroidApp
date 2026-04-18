# § 2 — Server Architecture

## Express Application

`server.js` is the single entry point. It combines:
- **Static file server** — Serves `public/` for the dashboard
- **REST API** — JSON endpoints for service management
- **Process Registry** — In-memory `Map<serviceId, RegistryEntry>` tracking spawned processes
- **Health Monitor** — HTTP polling to determine if services are alive
- **Auto-Restart Engine** — Watches for process exits and restarts with configurable delay

## Process Registry

```
Map<string, {
  proc: ChildProcess,    // The spawned process handle
  startedAt: string,     // ISO timestamp of last start
  restartCount: number,  // Cumulative restart count
  autoRestartEnabled: boolean  // Whether auto-restart is active
}>
```

Key behaviors:
- `startService()` — Spawns the process, pipes stdout/stderr to log, sets up exit handler
- `stopService()` — Disables auto-restart, kills process tree via `taskkill /T /F`
- Exit handler — If `autoRestartEnabled`, schedules restart after `restartDelay` ms

## Health Check

`checkHealth(url, timeout)` makes an HTTP GET to the service's health check URL.
- Returns `true` if any HTTP response is received (any status code)
- Returns `false` on connection error, timeout, or exception
- Timeout default: 4000ms

## Status Resolution

| Condition | Status |
|-----------|--------|
| Health check passes | `running` |
| Process managed but health check fails | `starting` |
| No managed process | `stopped` |

## Logging

All logs go to both `console.log` and `service-manager.log` (2 MB rotation).
Format: `[ISO_TIMESTAMP] message`

## Boot Sequence

1. Load `services.json`
2. Start Express on configured port (default 3500), bind `0.0.0.0`
3. Log dashboard URL with LAN IP
4. For each service with `autoRestart: true`:
   a. If `healthCheck` URL responds → skip (already running externally)
   b. Otherwise → `startService()`
