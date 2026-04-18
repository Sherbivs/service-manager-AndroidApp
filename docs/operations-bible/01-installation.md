# § 1 — Installation

## Prerequisites

- **Node.js** 18+ (LTS recommended)
- **Windows** (primary target — process management uses `taskkill`)

## Quick Start

```bash
npm install
npm start
```

Dashboard opens at `http://localhost:3500` (or your LAN IP).

## Auto-Start on Windows Boot

### Option A: Windows Scheduled Task (Recommended)

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

### Option B: node-windows (Windows Service)

```bash
npm run service:install
```

This registers Service Manager as a native Windows service that starts automatically and runs without a console window. Use `npm run service:uninstall` to remove.

## Verify Installation

1. Open `http://localhost:3500` in a browser
2. Check that the connection dot (top-right) is green
3. System bar shows hostname, IP, Node version, memory
