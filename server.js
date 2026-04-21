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
const { DatabaseSync } = require('node:sqlite');

const CONFIG_PATH    = path.join(__dirname, 'services.json');
const LOG_PATH       = path.join(__dirname, 'service-manager.log');
const ARCHIVE_DB_PATH = path.join(__dirname, 'logs-archive.db');
const MAX_LOG_SIZE   = 2 * 1024 * 1024; // 2 MB
const MAX_LOG_LINES  = 1000;            // lines kept in live log file
const DEFAULT_HEALTH_INTERVAL = 5000;
const MAX_HEALTH_HISTORY = 60; // ~5 min at 5s intervals

/* --------------------------------------------------------------------------
   .env loader — reads KEY=VALUE pairs into process.env (gitignored)
   -------------------------------------------------------------------------- */

(function loadEnv() {
  const envPath = path.join(__dirname, '.env');
  if (!fs.existsSync(envPath)) return;
  const lines = fs.readFileSync(envPath, 'utf8').split('\n');
  for (const raw of lines) {
    const line = raw.trim();
    if (!line || line.startsWith('#')) continue;
    const idx = line.indexOf('=');
    if (idx < 1) continue;
    const key = line.slice(0, idx).trim();
    const val = line.slice(idx + 1).trim();
    if (!process.env[key]) process.env[key] = val;
  }
})();

/* --------------------------------------------------------------------------
   Log Archive — SQLite (node:sqlite built-in, Node 22+, zero extra deps)
   -------------------------------------------------------------------------- */

let archiveDb = null;
try {
  archiveDb = new DatabaseSync(ARCHIVE_DB_PATH);
  archiveDb.exec(`
    CREATE TABLE IF NOT EXISTS log_archive (
      id          INTEGER PRIMARY KEY AUTOINCREMENT,
      service_id  TEXT    NOT NULL,
      line        TEXT    NOT NULL,
      archived_at INTEGER NOT NULL
    );
    CREATE INDEX IF NOT EXISTS idx_la_service ON log_archive(service_id);
    CREATE INDEX IF NOT EXISTS idx_la_time    ON log_archive(service_id, archived_at);
  `);
} catch (err) {
  console.error('[archive] Failed to open log archive DB:', err.message);
}

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
   Log Rotation — trims log file to MAX_LOG_LINES, archives excess to SQLite
   -------------------------------------------------------------------------- */

function rotateServiceLog(service) {
  if (!service.logFile || !archiveDb) return;
  const logPath = service.logFile;
  if (!fs.existsSync(logPath)) return;
  try {
    const content = fs.readFileSync(logPath, 'utf8');
    // Keep only timestamped event lines — strips ASCII box art, blank lines, bare URLs
    const lines = content.split('\n')
      .filter(l => l.trim() && /\d{2}:\d{2}:\d{2}/.test(l));
    if (lines.length <= MAX_LOG_LINES) return;

    const toArchive = lines.slice(0, lines.length - MAX_LOG_LINES);
    const toKeep    = lines.slice(-MAX_LOG_LINES);

    const insert = archiveDb.prepare(
      'INSERT INTO log_archive (service_id, line, archived_at) VALUES (?, ?, ?)'
    );
    const now = Date.now();
    archiveDb.exec('BEGIN');
    try {
      for (const line of toArchive) insert.run(service.id, line, now);
      archiveDb.exec('COMMIT');
    } catch (err) {
      archiveDb.exec('ROLLBACK');
      throw err;
    }

    // Attempt to trim the live log file. On Windows the file may be locked
    // while the service process holds it open for append (EBUSY). In that
    // case the archive already succeeded; the trim will happen next rotation.
    try {
      fs.writeFileSync(logPath, toKeep.join('\n') + '\n', 'utf8');
      log(`Log rotation: archived ${toArchive.length} lines from ${service.id}, kept ${MAX_LOG_LINES}`);
    } catch (writeErr) {
      if (writeErr.code === 'EBUSY' || writeErr.code === 'EPERM') {
        log(`Log rotation: archived ${toArchive.length} lines from ${service.id} (trim deferred — file in use)`);
      } else {
        throw writeErr;
      }
    }
  } catch (err) {
    log(`WARN: Log rotation failed for ${service.id}: ${err.message}`);
  }
}

/* --------------------------------------------------------------------------
   Process Registry & Health History
   -------------------------------------------------------------------------- */

const registry = new Map();
const healthHistory = new Map(); // id -> { checks: [], consecutiveFailures: 0 }

/* --------------------------------------------------------------------------
   Health Check (Enhanced - returns latency, status code, reachability)
   -------------------------------------------------------------------------- */

function checkHealth(url, timeout = 4000) {
  return new Promise((resolve) => {
    const start = Date.now();
    try {
      const mod = url.startsWith('https') ? require('https') : http;
      const req = mod.get(url, { timeout }, (res) => {
        res.resume();
        const latency = Date.now() - start;
        resolve({ reachable: true, statusCode: res.statusCode, latency, timestamp: Date.now() });
      });
      req.on('error', () => resolve({ reachable: false, statusCode: null, latency: Date.now() - start, timestamp: Date.now() }));
      req.on('timeout', () => { req.destroy(); resolve({ reachable: false, statusCode: null, latency: Date.now() - start, timestamp: Date.now() }); });
    } catch {
      resolve({ reachable: false, statusCode: null, latency: Date.now() - start, timestamp: Date.now() });
    }
  });
}

function isHealthy(check, expectedStatus) {
  if (!check.reachable) return false;
  if (expectedStatus && expectedStatus.length > 0) {
    return expectedStatus.includes(check.statusCode);
  }
  return check.statusCode >= 200 && check.statusCode < 400;
}

/* --------------------------------------------------------------------------
   Background Health Monitor
   -------------------------------------------------------------------------- */

let healthInterval = null;

async function runHealthChecks() {
  let config;
  try { config = loadConfig(); } catch { return; }

  for (const service of config.services) {
    if (!service.healthCheck) continue;

    const check = await checkHealth(service.healthCheck);
    check.healthy = isHealthy(check, service.expectedStatus);

    const history = healthHistory.get(service.id) || { checks: [], consecutiveFailures: 0 };
    history.checks.push(check);
    if (history.checks.length > MAX_HEALTH_HISTORY) history.checks.shift();

    if (!check.healthy) {
      history.consecutiveFailures++;
      const maxFail = service.maxHealthFailures || 5;
      const entry = registry.get(service.id);

      if (history.consecutiveFailures >= maxFail && entry?.autoRestartEnabled && !entry._healthRestarting) {
        entry._healthRestarting = true;
        log(`[${service.id}] Health failed ${history.consecutiveFailures}x - restarting`);
        stopService(service.id);
        setTimeout(() => {
          startService(service);
          history.consecutiveFailures = 0;
        }, service.restartDelay || 5000);
      }
    } else {
      history.consecutiveFailures = 0;
    }

    healthHistory.set(service.id, history);
  }
}

function startHealthMonitor(intervalMs) {
  if (healthInterval) clearInterval(healthInterval);
  healthInterval = setInterval(runHealthChecks, intervalMs);
  runHealthChecks();
}

/* --------------------------------------------------------------------------
   Service Status (Enhanced - includes latency, health %, metrics)
   -------------------------------------------------------------------------- */

async function getStatus(service) {
  const entry = registry.get(service.id);
  const managed = !!(entry && entry.proc && !entry.proc.killed);
  const history = healthHistory.get(service.id);
  const lastCheck = history?.checks?.length ? history.checks[history.checks.length - 1] : null;

  let alive = false;
  if (lastCheck) {
    alive = lastCheck.reachable;
  } else if (service.healthCheck) {
    const check = await checkHealth(service.healthCheck);
    alive = check.reachable;
  } else if (managed) {
    alive = true;
  }

  let status = 'stopped';
  if (alive) status = 'running';
  else if (managed) status = 'starting';

  // Compute rolling metrics
  let healthPercent = null;
  let avgLatency = null;

  if (history?.checks?.length > 0) {
    const checks = history.checks;
    healthPercent = Math.round((checks.filter(c => c.healthy).length / checks.length) * 100);
    const reachable = checks.filter(c => c.reachable);
    if (reachable.length > 0) {
      avgLatency = Math.round(reachable.reduce((s, c) => s + c.latency, 0) / reachable.length);
    }
  }

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
    restartCount: entry?.restartCount || 0,
    lastCheck: lastCheck ? {
      latency: lastCheck.latency,
      statusCode: lastCheck.statusCode,
      healthy: lastCheck.healthy,
      reachable: lastCheck.reachable,
      timestamp: lastCheck.timestamp
    } : null,
    healthPercent,
    avgLatency,
    consecutiveFailures: history?.consecutiveFailures || 0
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

  const resolvedArgs = (service.args || []).map(arg =>
    arg.replace(/\$\{([^}]+)\}/g, (_, key) => process.env[key] || '')
  );

  const proc = spawn(service.command, resolvedArgs, {
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

  // Reset health tracking on fresh start
  healthHistory.set(service.id, { checks: [], consecutiveFailures: 0 });

  registry.set(service.id, entry);
  log(`Started ${service.name} - PID ${proc.pid}`);
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

// --- API: Health history for a service ---
app.get('/api/services/:id/health', (req, res) => {
  const history = healthHistory.get(req.params.id);
  if (!history) return res.json({ checks: [], consecutiveFailures: 0 });
  res.json(history);
});

// --- API: Start all services ---
app.post('/api/services/start-all', (req, res) => {
  try {
    const config = loadConfig();
    const results = config.services.map(s => ({ id: s.id, ...startService(s) }));
    res.json({ success: true, results });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Stop all services ---
app.post('/api/services/stop-all', (_req, res) => {
  try {
    const config = loadConfig();
    const results = config.services.map(s => ({ id: s.id, ...stopService(s.id) }));
    res.json({ success: true, results });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Service log file (last N lines) ---
app.get('/api/services/:id/logs', (req, res) => {
  try {
    const config = loadConfig();
    const service = config.services.find(s => s.id === req.params.id);
    if (!service) return res.status(404).json({ error: 'Service not found' });
    if (!service.logFile) return res.json({ lines: [], logFile: null });

    const logPath = service.logFile;
    if (!fs.existsSync(logPath)) return res.json({ lines: [], logFile: logPath });

    const lines = parseInt(req.query.lines) || 100;
    const content = fs.readFileSync(logPath, 'utf8');
    const all = content.split('\n').filter(l => l.trim());
    res.json({ lines: all.slice(-lines), logFile: logPath });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Service env keys (names only, never values) ---
app.get('/api/services/:id/env', (req, res) => {
  try {
    const config = loadConfig();
    const service = config.services.find(s => s.id === req.params.id);
    if (!service) return res.status(404).json({ error: 'Service not found' });
    res.json({ envKeys: service.envKeys || [], envFile: service.envFile || null });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Update a single env key ---
app.post('/api/services/:id/env', (req, res) => {
  try {
    const config = loadConfig();
    const service = config.services.find(s => s.id === req.params.id);
    if (!service) return res.status(404).json({ error: 'Service not found' });

    const { key, value } = req.body;
    if (!key || typeof value === 'undefined') return res.status(400).json({ error: 'key and value required' });

    // Only allow keys declared in envKeys for this service
    if (!service.envKeys || !service.envKeys.includes(key)) {
      return res.status(403).json({ error: `Key '${key}' not permitted for this service` });
    }

    const envPath = service.envFile || path.join(__dirname, '.env');

    // Read existing .env lines
    let lines = [];
    if (fs.existsSync(envPath)) {
      lines = fs.readFileSync(envPath, 'utf8').split('\n');
    }

    // Update or append
    const idx = lines.findIndex(l => l.split('=')[0].trim() === key);
    const newLine = `${key}=${value}`;
    if (idx >= 0) lines[idx] = newLine;
    else lines.push(newLine);

    fs.writeFileSync(envPath, lines.join('\n').trimEnd() + '\n', 'utf8');

    // Also update live process.env so restarts pick it up
    process.env[key] = value;

    log(`Env updated: ${key} (service: ${req.params.id})`);
    res.json({ success: true });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- API: Service log archive (SQLite, searchable, paginated) ---
app.get('/api/services/:id/logs/archive', (req, res) => {
  if (!archiveDb) return res.status(503).json({ error: 'Archive DB not available' });
  try {
    const config = loadConfig();
    const service = config.services.find(s => s.id === req.params.id);
    if (!service) return res.status(404).json({ error: 'Service not found' });

    const limit  = Math.min(parseInt(req.query.limit)  || 100, 500);
    const offset = parseInt(req.query.offset) || 0;
    const q      = (req.query.q || '').trim();

    let rows, total;
    if (q) {
      rows  = archiveDb.prepare(
        `SELECT id, line, archived_at FROM log_archive WHERE service_id = ? AND line LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?`
      ).all(req.params.id, `%${q}%`, limit, offset);
      total = archiveDb.prepare(
        `SELECT COUNT(*) AS n FROM log_archive WHERE service_id = ? AND line LIKE ?`
      ).get(req.params.id, `%${q}%`).n;
    } else {
      rows  = archiveDb.prepare(
        `SELECT id, line, archived_at FROM log_archive WHERE service_id = ? ORDER BY id DESC LIMIT ? OFFSET ?`
      ).all(req.params.id, limit, offset);
      total = archiveDb.prepare(
        `SELECT COUNT(*) AS n FROM log_archive WHERE service_id = ?`
      ).get(req.params.id).n;
    }

    res.json({ rows, total, limit, offset });
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
  log(`Service Manager v1.1 - http://0.0.0.0:${PORT}`);
  log(`Dashboard: http://${localIp}:${PORT}`);
  log(`Managing ${config.services.length} service(s)`);
  log(`Health monitor: every ${config.healthCheckInterval || DEFAULT_HEALTH_INTERVAL}ms`);
  log('================================================');

  // Auto-start services marked autoStart
  for (const s of config.services.filter(s => s.autoStart)) {
    if (s.healthCheck) {
      const check = await checkHealth(s.healthCheck);
      if (check.reachable && isHealthy(check, s.expectedStatus)) {
        log(`${s.name} already running (healthy - ${check.latency}ms)`);
        continue;
      }
    }
    log(`Auto-starting: ${s.name}`);
    startService(s);
  }

  // Start background health monitor
  startHealthMonitor(config.healthCheckInterval || DEFAULT_HEALTH_INTERVAL);

  // Rotate service logs on boot, then every 5 minutes
  const allServices = loadConfig().services;
  for (const s of allServices.filter(s => s.logFile)) rotateServiceLog(s);
  setInterval(() => {
    const cfg = loadConfig();
    for (const s of cfg.services.filter(s => s.logFile)) rotateServiceLog(s);
  }, 5 * 60 * 1000);

  // Launch system tray icon
  const trayScript = path.join(__dirname, 'tray.ps1');
  if (fs.existsSync(trayScript)) {
    const trayProc = spawn('powershell.exe', [
      '-ExecutionPolicy', 'Bypass',
      '-WindowStyle', 'Hidden',
      '-File', trayScript
    ], { cwd: __dirname, stdio: 'ignore', detached: true, windowsHide: true });
    trayProc.unref();
    log('System tray icon launched');
  }
});

/* --------------------------------------------------------------------------
   Graceful Shutdown
   -------------------------------------------------------------------------- */

function gracefulShutdown(signal) {
  log(`Received ${signal} - shutting down`);
  if (healthInterval) clearInterval(healthInterval);

  for (const [id, entry] of registry) {
    if (entry?.proc) {
      entry.autoRestartEnabled = false;
      try { exec(`taskkill /PID ${entry.proc.pid} /T /F`); } catch { /* best effort */ }
    }
  }

  setTimeout(() => process.exit(0), 2000);
}

process.on('SIGINT', () => gracefulShutdown('SIGINT'));
process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
