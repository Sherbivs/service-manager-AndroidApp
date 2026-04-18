# § 2 — Configuration

## services.json Schema

```json
{
  "port": 3500,
  "services": [
    {
      "id": "unique-id",
      "name": "Display Name",
      "project": "Project Group",
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

## Field Reference

| Field | Required | Type | Default | Description |
|-------|----------|------|---------|-------------|
| `id` | Yes | string | — | Unique identifier (used in API URLs) |
| `name` | Yes | string | — | Display name in dashboard |
| `project` | No | string | `""` | Project grouping label |
| `description` | No | string | `""` | Short description shown on card |
| `type` | No | string | `"process"` | `"web"` or `"process"` |
| `command` | Yes | string | — | Executable to run |
| `args` | No | string[] | `[]` | Command-line arguments |
| `workingDirectory` | No | string | server.js dir | Working directory for the process |
| `healthCheck` | No | string | — | URL to poll for liveness |
| `url` | No | string | — | URL shown as "Open" link in dashboard |
| `autoRestart` | No | boolean | `false` | Restart on crash |
| `restartDelay` | No | number | `5000` | Milliseconds before restart |

## Hot Reload

`services.json` is re-read on every API request. You can add or modify services without restarting the manager.

## Port Configuration

The `port` field in `services.json` controls which port the dashboard listens on. Default: `3500`. The server binds to `0.0.0.0` for LAN access.
