# Service Manager — Android App

Native Android companion app for the [Service Manager](https://github.com/Sherbivs/service-manager) dashboard. Monitor and control your local dev services from any Android device on your LAN.

## Features

- **Live service status** — see running/stopped state, health, PID, and uptime
- **One-tap control** — Start, Stop, Restart any managed service
- **System overview** — hostname, IP, memory, Node version, uptime
- **Log viewer** — tail live logs and search the archive
- **Configurable server URL** — point to any Service Manager instance on your LAN
- **Secure storage** — server URL and credentials stored in EncryptedSharedPreferences
- **Material 3 design** — dark-theme ready, responsive layout

## Requirements

- Android 7.0+ (API 24)
- Service Manager server running at `http://<host>:3500` ([Sherbivs/service-manager](https://github.com/Sherbivs/service-manager))
- Android Studio Hedgehog 2023.1.1+ (for development)

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

Or open in Android Studio and press **Run**.

## Configuration

On first launch, enter your Service Manager server URL (e.g. `http://192.168.1.100:3500`). This is stored securely via `EncryptedSharedPreferences` and never hardcoded.

## Architecture

```
Android App (Kotlin, MVVM)
  ↕ Retrofit / OkHttp
Service Manager API  (http://<host>:3500)
  ↕
Managed Service Processes
```

**Stack:** Kotlin, MVVM, Retrofit, OkHttp, ViewBinding, Material 3, `androidx.security.crypto`

## Project Structure

```
app/src/main/
  java/com/servicemanager/app/
    ui/          — Activities, Fragments, ViewModels
    data/        — API client, models, repository
    util/        — Shared helpers
  res/           — Layouts, drawables, strings, themes
  AndroidManifest.xml
```

## Security

- All secrets stored in Android Keystore / EncryptedSharedPreferences
- Network security configured via `network_security_config.xml`
- Release builds use R8 minification + resource shrinking
- `android:debuggable="false"` enforced in release
- Keystore files and `keystore.properties` are gitignored

## Companion Repository

The Node.js service manager server lives at **[Sherbivs/service-manager](https://github.com/Sherbivs/service-manager)**.
This app is the Android client only — it has no server-side components.

## License

MIT


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
