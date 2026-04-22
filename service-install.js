/* =============================================================================
   Service Manager — Startup Installer
   - Server: registered as a Task Scheduler task with crash-restart (always running).
   - Tray:   Startup folder shortcut (starts once on login, no restart needed).
   Usage:  npm run service:install
   ============================================================================= */

const path = require('path');
const fs   = require('fs');
const { exec } = require('child_process');

const STARTUP_DIR = path.join(
  process.env.APPDATA,
  'Microsoft', 'Windows', 'Start Menu', 'Programs', 'Startup'
);

const TASK_NAME = 'ServiceManagerServer';

/* ---- Register Task Scheduler task for the server (handles boot + crash restart) ---- */

function installScheduledTask(launchNow) {
  const vbsPath = path.join(__dirname, 'watchdog-server.vbs');
  const workDir = __dirname;

  if (!fs.existsSync(vbsPath)) {
    console.log('[!] watchdog-server.vbs not found — skipping server task.');
    return;
  }

  // Task Scheduler minimum restart interval is 1 minute — but the watchdog VBS
  // itself loops and restarts node within 10 seconds of a crash.
  // Task Scheduler's 1-minute restart is a last-resort safety net if the watchdog itself dies.
  const ps = `
$action   = New-ScheduledTaskAction -Execute 'wscript.exe' -Argument ('//Nologo "' + '${vbsPath.replace(/\\/g, '\\\\')}' + '"') -WorkingDirectory '${workDir.replace(/\\/g, '\\\\')}';
$trigger  = New-ScheduledTaskTrigger -AtLogOn -User '$env:USERNAME';
$settings = New-ScheduledTaskSettingsSet \`
  -RestartCount 9999 \`
  -RestartInterval (New-TimeSpan -Seconds 10) \`
  -ExecutionTimeLimit ([TimeSpan]::Zero) \`
  -MultipleInstances IgnoreNew;
Register-ScheduledTask -TaskName '${TASK_NAME}' -Action $action -Trigger $trigger -Settings $settings -RunLevel Highest -Force | Out-Null;
Write-Host 'Task registered.'
`.trim();

  exec(`powershell -NoProfile -Command "${ps.replace(/"/g, '\\"').replace(/\n/g, ' ')}"`, (err, stdout) => {
    if (err) {
      console.log(`[!] Could not register scheduled task: ${err.message}`);
      return;
    }
    console.log(`[+] Task Scheduler task "${TASK_NAME}" registered — starts at login and restarts on crash.`);

    // Remove old Startup shortcut if left over from a previous install
    const oldShortcut = path.join(STARTUP_DIR, 'ServiceManagerServer.lnk');
    if (fs.existsSync(oldShortcut)) {
      try { fs.unlinkSync(oldShortcut); console.log('[+] Removed old ServiceManagerServer.lnk from Startup folder.'); }
      catch { /* non-fatal */ }
    }

    if (launchNow) {
      exec(`wscript.exe "${vbsPath}"`, { cwd: __dirname }, (e) => {
        if (e) console.log(`[!] Could not launch watchdog now: ${e.message}`);
        else   console.log('[+] Server watchdog launched now (10-second crash restart).');
      });
    }
  });
}

/* ---- Startup folder shortcut for the tray icon ---- */

function installTrayShortcut(launchNow) {
  const vbsSource    = path.join(__dirname, 'launch-tray.vbs');
  const shortcutDest = path.join(STARTUP_DIR, 'ServiceManagerTray.lnk');

  if (!fs.existsSync(vbsSource)) {
    console.log('[!] launch-tray.vbs not found — skipping tray shortcut.');
    return;
  }

  const vbsEsc  = vbsSource.replace(/'/g, "''");
  const dirEsc  = __dirname.replace(/'/g, "''");
  const destEsc = shortcutDest.replace(/'/g, "''");

  const ps = [
    `$ws = New-Object -ComObject WScript.Shell`,
    `$sc = $ws.CreateShortcut('${destEsc}')`,
    `$sc.TargetPath = 'wscript.exe'`,
    `$sc.Arguments = '"${vbsEsc}"'`,
    `$sc.WorkingDirectory = '${dirEsc}'`,
    `$sc.Description = 'Service Manager Tray Icon'`,
    `$sc.Save()`
  ].join('; ');

  exec(`powershell -NoProfile -Command "${ps.replace(/"/g, '\\"')}"`, (err) => {
    if (err) {
      console.log(`[!] Could not create tray shortcut: ${err.message}`);
      return;
    }
    console.log('[+] ServiceManagerTray.lnk → Startup folder.');

    if (launchNow) {
      exec(`wscript.exe "${vbsSource}"`, { cwd: __dirname }, (e) => {
        if (e) console.log(`[!] Could not launch tray now: ${e.message}`);
        else   console.log('[+] Tray icon launched now.');
      });
    }
  });
}

/* ---- Install ---- */

console.log('Installing Service Manager...\n');

// 1. Server — Task Scheduler task (starts at login + restarts on crash automatically)
installScheduledTask(true);

// 2. Tray icon — Startup folder shortcut
installTrayShortcut(true);

console.log('\nDone. Service Manager server will always restart on crash.');
console.log('To remove: npm run service:uninstall');
