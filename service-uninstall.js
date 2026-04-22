/* =============================================================================
   Service Manager — Startup Uninstaller
   Removes the Task Scheduler task (server) and Startup folder shortcut (tray).
   Usage:  npm run service:uninstall
   ============================================================================= */

const path = require('path');
const fs   = require('fs');
const { exec } = require('child_process');

const STARTUP_DIR = path.join(
  process.env.APPDATA,
  'Microsoft', 'Windows', 'Start Menu', 'Programs', 'Startup'
);

const TASK_NAME = 'ServiceManagerServer';

console.log('Removing Service Manager auto-start...\n');

// 1. Remove scheduled task (server crash-restart)
exec(
  `powershell -NoProfile -Command "Unregister-ScheduledTask -TaskName '${TASK_NAME}' -Confirm:$false -ErrorAction SilentlyContinue; Write-Host done"`,
  (err, stdout) => {
    if (err) console.log(`[!] Could not remove scheduled task: ${err.message}`);
    else     console.log(`[+] Scheduled task "${TASK_NAME}" removed.`);
  }
);

// 2. Remove any leftover server Startup shortcut (legacy)
const serverShortcut = path.join(STARTUP_DIR, 'ServiceManagerServer.lnk');
if (fs.existsSync(serverShortcut)) {
  try { fs.unlinkSync(serverShortcut); console.log('[+] ServiceManagerServer.lnk removed.'); }
  catch (err) { console.log(`[!] Could not remove ServiceManagerServer.lnk: ${err.message}`); }
} else {
  console.log('[=] ServiceManagerServer.lnk not present (already removed).');
}

// 3. Remove tray Startup shortcut
const trayShortcut = path.join(STARTUP_DIR, 'ServiceManagerTray.lnk');
if (fs.existsSync(trayShortcut)) {
  try { fs.unlinkSync(trayShortcut); console.log('[+] ServiceManagerTray.lnk removed.'); }
  catch (err) { console.log(`[!] Could not remove ServiceManagerTray.lnk: ${err.message}`); }
} else {
  console.log('[=] ServiceManagerTray.lnk not present (already removed).');
}

console.log('\nDone. Service Manager will no longer auto-start on login.');
console.log('To reinstall: npm run service:install');
