/* =============================================================================
   Service Manager — server.js
   Elastic service orchestrator with web dashboard.
   Manages local dev services, health monitoring, and auto-restart.
   ============================================================================= */

const express = require('express');
const { spawn, exec } = require('child_process');
const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');

const CONFIG_PATH = path.join(__dirname, 'services.json');
const LOG_PATH = path.join(__dirname, 'service-manager.log');
const MAX_LOG_SIZE = 2 * 1024 * 1024; // 2 MB

/* --------------------------------------------------------------------------
   Logging
   -------------------------------------------------------------------------- */

function log(msg) {
  const line = `[${new Date().toISOString()}] ${msg}`;
  console.log(line);
  try {
    if (fs.existsSync(LOG_PATH) && fs.statSync(LOG_PATH).size > MAX_LOG_SIZE) {
      fs.renameSync(LOG_PATH, LOG_PATH + '.old');
    }
    fs.appendFileSync(LOG_PATH, line + '\n');
  } catch { /* logging should never crash the server */ }
}

/* --------------------------------------------------------------------------
   Config
   -------------------------------------------------------------------------- */

function loadConfig() {
  return JSON.parse(fs.readFileSync(CONFIG_PATH, 'utf8'));
}

/* --------------------------------------------------------------------------
   Process Registry
   -------------------------------------------------------------------------- */

const registry = new Map();

/* --------------------------------------------------------------------------
   Health Check
   -------------------------------------------------------------------------- */

function checkHealth(url, timeout = 4000) {
  return new Promise((resolve) => {
    try {
      const mod = url.startsWith('https') ? require('https') : http;
      const req = mod.get(url, { timeout }, (res) => {
        res.resume();
        resolve(true);
      });
      req.on('error', () => resolve(false));
      req.on('timeout', () => { req.destroy(); resolve(false); });
    } catch {
      resolve(false);
    }
  });
}

/* --------------------------------------------------------------------------
   Service Status
   -------------------------------------------------------------------------- */

async function getStatus(service) {
  const entry = registry.get(service.id);
  const managed = !!(entry && entry.proc && !entry.proc.killed);
  let alive = false;

  if (service.healthCheck) {
    alive = await checkHealth(service.healthCheck);
  } else if (managed) {
    alive = true;
  }

  let status = 'stopped';
  if (alive) status = 'running';
  else if (managed) status = 'starting';

  return {
    id: service.id,
    name: service.name,
    project: service.project || '',
    description: service.description || '',
    type: service.type || 'process',
    url: service.url || null,
    healthCheck: service.healthCheck || null,
    autoRestart: !!service.autoRestart,
    status,
    managed,
    pid: entry?.proc?.pid || null,
    startedAt: entry?.startedAt || null,
    restartCount: entry?.restartCount || 0
  };
}

/* --------------------------------------------------------------------------
   Start Service
   -------------------------------------------------------------------------- */

function startService(service) {
  const existing = registry.get(service.id);
  if (existing?.proc && !existing.proc.killed) {
    return { success: false, message: 'Already running' };
  }

  log(`Starting: ${service.name} (${service.id})`);

  const proc = spawn(service.command, service.args || [], {
    cwd: service.workingDirectory || __dirname,
    stdio: ['ignore', 'pipe', 'pipe'],
    windowsHide: true,
    shell: false
  });

  const entry = {
    proc,
    startedAt: new Date().toISOString(),
    restartCount: existing?.restartCount || 0,
    autoRestartEnabled: !!service.autoRestart
  };

  proc.stdout?.on('data', (d) => {
    d.toString().trim().split('\n').forEach(l => log(`[${service.id}] ${l}`));
  });
  proc.stderr?.on('data', (d) => {
    d.toString().trim().split('\n').forEach(l => log(`[${service.id}:err] ${l}`));
  });

  proc.on('exit', (code) => {
    log(`[${service.id}] Exited (code ${code})`);
    if (entry.autoRestartEnabled) {
      entry.restartCount++;
      const delay = service.restartDelay || 5000;
      log(`[${service.id}] Auto-restart #${entry.restartCount} in ${delay}ms`);
      setTimeout(() => {
        const current = registry.get(service.id);
        if (current?.autoRestartEnabled) {
          startService(service);
        }
      }, delay);
    }
  });

  proc.on('error', (err) => {
    log(`[${service.id}] Process error: ${err.message}`);
  });

  registry.set(service.id, entry);
  log(`Started ${service.name} — PID ${proc.pid}`);
  return { success: true, pid: proc.pid };
}

/* --------------------------------------------------------------------------
   Stop Service
   -------------------------------------------------------------------------- */

function stopService(serviceId) {
  const entry = registry.get(serviceId);
  if (!entry?.proc) {
    return { success: false, message: 'Not managed by this instance' };
  }

  log(`Stopping: ${serviceId} (PID ${entry.proc.pid})`);
  entry.autoRestartEnabled = false;

  try {
    exec(`taskkill /PID ${entry.proc.pid} /T /F`, (err) => {
      if (err) log(`taskkill warning for ${serviceId}: ${err.message}`);
    });
  } catch (e) {
    log(`Stop error for ${serviceId}: ${e.message}`);
  }

  registry.delete(serviceId);
  return { success: true };
}

/* --------------------------------------------------------------------------
   Express App
   -------------------------------------------------------------------------- */

const app = express();
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// --- API: List all services with live status ---
app.get('/api/services', async (_req, res) => {
  try {
    const config = loadConfig();
    const statuses = await Promise.all(config.services.map(getStatus));
    res.json(statuses);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Start ---
app.post('/api/services/:id/start', (req, res) => {
  try {
    const config = loadConfig();
    const service = config.services.find(s => s.id === req.params.id);
    if (!service) return res.status(404).json({ error: 'Service not found' });
    res.json(startService(service));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Stop ---
app.post('/api/services/:id/stop', (req, res) => {
  try {
    res.json(stopService(req.params.id));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Restart ---
app.post('/api/services/:id/restart', async (req, res) => {
  try {
    const config = loadConfig();
    const service = config.services.find(s => s.id === req.params.id);
    if (!service) return res.status(404).json({ error: 'Service not found' });
    stopService(req.params.id);
    await new Promise(r => setTimeout(r, 2500));
    res.json(startService(service));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: System info ---
app.get('/api/system', (_req, res) => {
  const ifaces = os.networkInterfaces();
  const localIp = Object.values(ifaces).flat()
    .find(i => i.family === 'IPv4' && !i.internal)?.address || '127.0.0.1';

  res.json({
    hostname: os.hostname(),
    platform: os.platform(),
    localIp,
    uptime: process.uptime(),
    nodeVersion: process.version,
    memory: {
      total: os.totalmem(),
      free: os.freemem(),
      used: os.totalmem() - os.freemem()
    }
  });
});

// --- API: Logs (last N lines) ---
app.get('/api/logs', (req, res) => {
  try {
    const lines = parseInt(req.query.lines) || 50;
    if (!fs.existsSync(LOG_PATH)) return res.json([]);
    const content = fs.readFileSync(LOG_PATH, 'utf8');
    const all = content.trim().split('\n');
    res.json(all.slice(-lines));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/* --------------------------------------------------------------------------
   Boot
   -------------------------------------------------------------------------- */

const config = loadConfig();
const PORT = config.port || 3500;

app.listen(PORT, '0.0.0.0', async () => {
  const ifaces = os.networkInterfaces();
  const localIp = Object.values(ifaces).flat()
    .find(i => i.family === 'IPv4' && !i.internal)?.address || '127.0.0.1';

  log('================================================');
  log(`Service Manager v1.0 — http://0.0.0.0:${PORT}`);
  log(`Dashboard: http://${localIp}:${PORT}`);
  log(`Managing ${config.services.length} service(s)`);
  log('================================================');

  // Auto-start services marked autoRestart
  for (const s of config.services.filter(s => s.autoRestart)) {
    if (s.healthCheck) {
      const alive = await checkHealth(s.healthCheck);
      if (alive) {
        log(`${s.name} already running (discovered via health check)`);
        continue;
      }
    }
    log(`Auto-starting: ${s.name}`);
    startService(s);
  }
});
