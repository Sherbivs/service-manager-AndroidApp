/* =============================================================================
   Service Manager — Windows Service Uninstaller
   Removes the Windows service and tray auto-start shortcut.
   Usage:  npm run service:uninstall
   ============================================================================= */

const path = require('path');
const fs = require('fs');
const Service = require('node-windows').Service;

const svc = new Service({
  name: 'ServiceManager',
  script: path.join(__dirname, 'server.js')
});

/* ---- Service Events ---- */

svc.on('uninstall', () => {
  console.log('[+] Windows service removed.');
  removeTrayAutoStart();
});

svc.on('error', (err) => {
  console.error('[!] Error:', err);
});

/* ---- Remove Tray Auto-Start ---- */

function removeTrayAutoStart() {
  const shortcutPath = path.join(
    process.env.APPDATA,
    'Microsoft', 'Windows', 'Start Menu', 'Programs', 'Startup',
    'ServiceManagerTray.lnk'
  );

  try {
    if (fs.existsSync(shortcutPath)) {
      fs.unlinkSync(shortcutPath);
      console.log('[+] Tray auto-start shortcut removed.');
    }
  } catch (err) {
    console.log('[!] Could not remove tray shortcut:', err.message);
  }
}

/* ---- Run ---- */

console.log('Uninstalling Service Manager Windows service...\n');
svc.uninstall();
