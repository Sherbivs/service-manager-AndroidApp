/* =============================================================================
   Service Manager — Windows Service Installer
   Registers Service Manager as a native Windows service + tray auto-start.
   Usage:  npm run service:install
   ============================================================================= */

const path = require('path');
const fs = require('fs');
const { exec } = require('child_process');
const Service = require('node-windows').Service;

const svc = new Service({
  name: 'ServiceManager',
  description: 'Service Manager — elastic service management dashboard for local dev services',
  script: path.join(__dirname, 'server.js'),
  workingDirectory: __dirname,
  allowServiceLogon: true
});

/* ---- Service Events ---- */

svc.on('install', () => {
  console.log('[+] Windows service installed. Starting...');
  svc.start();
});

svc.on('start', () => {
  console.log('[+] Service Manager is running as a Windows service.');
  console.log('    Dashboard: http://localhost:3500');
  installTrayAutoStart();
});

svc.on('alreadyinstalled', () => {
  console.log('[=] Service Manager is already installed.');
  console.log('    Use "npm run service:uninstall" to remove, then reinstall.');
});

svc.on('error', (err) => {
  console.error('[!] Error:', err);
});

/* ---- Tray Auto-Start ---- */

function installTrayAutoStart() {
  const startupDir = path.join(
    process.env.APPDATA,
    'Microsoft', 'Windows', 'Start Menu', 'Programs', 'Startup'
  );
  const vbsSource = path.join(__dirname, 'launch-tray.vbs');
  const shortcutDest = path.join(startupDir, 'ServiceManagerTray.lnk');

  if (!fs.existsSync(vbsSource)) {
    console.log('[!] launch-tray.vbs not found — skipping tray auto-start.');
    return;
  }

  // Use PowerShell to create a shortcut
  const ps = `
    $ws = New-Object -ComObject WScript.Shell
    $sc = $ws.CreateShortcut('${shortcutDest.replace(/'/g, "''")}')
    $sc.TargetPath = 'wscript.exe'
    $sc.Arguments = '"${vbsSource.replace(/'/g, "''")}"'
    $sc.WorkingDirectory = '${__dirname.replace(/'/g, "''")}'
    $sc.Description = 'Service Manager Tray Icon'
    $sc.Save()
  `.trim();

  exec(`powershell -NoProfile -Command "${ps.replace(/"/g, '\\"')}"`, (err) => {
    if (err) {
      console.log('[!] Could not create tray startup shortcut:', err.message);
    } else {
      console.log('[+] Tray icon will auto-start on login.');
      // Also launch tray now
      exec(`wscript.exe "${vbsSource}"`, { cwd: __dirname });
      console.log('[+] Tray icon launched.');
    }
  });
}

/* ---- Run ---- */

console.log('Installing Service Manager as a Windows service...');
console.log('(You may see a UAC elevation prompt)\n');
svc.install();
