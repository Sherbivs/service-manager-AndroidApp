# Service Manager

Elastic service management dashboard — manage, monitor, and control local dev services from a single web UI.

## Features

- **Web dashboard** at `http://<your-ip>:3500` — dark theme, responsive, auto-refreshes every 5 seconds
- **Start / Stop / Restart** any configured service with one click
- **Health monitoring** via HTTP health checks with live status indicators
- **Auto-restart** — crashed services restart automatically with configurable delay
- **System info** — hostname, IP, Node version, memory usage, uptime
- **Toast notifications** — instant feedback on actions
- **Multi-project** — manage services from any number of projects in one place
- **Network accessible** — binds to `0.0.0.0`, reachable from any device on your LAN

## Quick Start

```bash
npm install
npm start
```

Open `http://localhost:3500` (or your LAN IP).

## Configuration

Edit `services.json` to add/remove services:

```json
{
  "port": 3500,
  "services": [
    {
      "id": "my-service",
      "name": "My Dev Server",
      "project": "My Project",
      "description": "What this service does",
      "type": "web",
      "command": "node",
      "args": ["server.js"],
      "workingDirectory": "C:\\path\\to\\project",
      "healthCheck": "http://127.0.0.1:8080",
      "url": "http://192.168.1.100:8080",
      "autoRestart": true,
      "restartDelay": 5000
    }
  ]
}
```

### Service Config Fields

| Field | Required | Description |
|-------|----------|-------------|
| `id` | Yes | Unique identifier |
| `name` | Yes | Display name |
| `project` | No | Project grouping label |
| `description` | No | Short description |
| `type` | No | `web` or `process` |
| `command` | Yes | Executable to run |
| `args` | No | Array of arguments |
| `workingDirectory` | No | CWD for the process |
| `healthCheck` | No | URL to poll for status |
| `url` | No | URL shown as "Open" link in dashboard |
| `autoRestart` | No | Auto-restart on crash (default: false) |
| `restartDelay` | No | Milliseconds before restart (default: 5000) |

## API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/services` | GET | List all services with live status |
| `/api/services/:id/start` | POST | Start a service |
| `/api/services/:id/stop` | POST | Stop a service |
| `/api/services/:id/restart` | POST | Restart a service |
| `/api/system` | GET | System info (hostname, IP, memory, uptime) |
| `/api/logs?lines=50` | GET | Recent log lines |

## Auto-Start on Boot (Windows)

Register as a scheduled task to run at login:

```powershell
$action = New-ScheduledTaskAction `
  -Execute "node.exe" `
  -Argument '"C:\path\to\service-manager\server.js"' `
  -WorkingDirectory "C:\path\to\service-manager"

$trigger = New-ScheduledTaskTrigger -AtLogOn -User "$env:USERDOMAIN\$env:USERNAME"

$settings = New-ScheduledTaskSettingsSet `
  -ExecutionTimeLimit ([TimeSpan]::Zero) `
  -RestartCount 10 `
  -RestartInterval (New-TimeSpan -Minutes 1) `
  -StartWhenAvailable

Register-ScheduledTask `
  -TaskName "ServiceManager" `
  -Action $action -Trigger $trigger -Settings $settings `
  -Description "Service Manager dashboard" -Force
```

## Stack

- Node.js + Express
- Vanilla HTML/CSS/JS (no build step)
- Dark theme with responsive grid layout

## License

MIT
