/* =============================================================================
   Service Manager — Dashboard App
   ============================================================================= */

const REFRESH_INTERVAL = 5000;
let countdown = 5;
let refreshTimer = null;
let countdownTimer = null;

/* --------------------------------------------------------------------------
   SVG Icons
   -------------------------------------------------------------------------- */

const ICONS = {
  play:    '<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3"/></svg>',
  stop:    '<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="4" y="4" width="16" height="16" rx="2"/></svg>',
  restart: '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 4v6h-6M1 20v-6h6"/><path d="M3.51 9a9 9 0 0114.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0020.49 15"/></svg>',
  link:    '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>',
  startAll:'<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="1 3 11 12 1 21"/><polygon points="13 3 23 12 13 21"/></svg>',
  stopAll: '<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><rect x="2" y="4" width="7" height="16" rx="1"/><rect x="15" y="4" width="7" height="16" rx="1"/></svg>',
  logs:    '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="8" y1="13" x2="16" y2="13"/><line x1="8" y1="17" x2="16" y2="17"/></svg>',
  settings:'<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/></svg>'
};

/* --------------------------------------------------------------------------
   API
   -------------------------------------------------------------------------- */

async function api(endpoint, method = 'GET') {
  try {
    const res = await fetch(`/api${endpoint}`, {
      method,
      headers: { 'Content-Type': 'application/json' }
    });
    return await res.json();
  } catch (err) {
    setConnected(false);
    throw err;
  }
}

/* --------------------------------------------------------------------------
   State
   -------------------------------------------------------------------------- */

let services = [];
let systemInfo = {};
let connected = true;

function setConnected(state) {
  connected = state;
  const dot = document.getElementById('connection-dot');
  dot.className = state ? 'topbar__dot' : 'topbar__dot topbar__dot--disconnected';
  dot.title = state ? 'Connected' : 'Disconnected';
}

/* --------------------------------------------------------------------------
   Fetch
   -------------------------------------------------------------------------- */

async function fetchServices() {
  try {
    services = await api('/services');
    setConnected(true);
    renderServices();
    renderSummary();
  } catch {
    setConnected(false);
  }
}

async function fetchSystem() {
  try {
    systemInfo = await api('/system');
    setConnected(true);
    renderSystemBar();
    renderHostInfo();
  } catch {
    setConnected(false);
  }
}

async function fetchAll() {
  await Promise.all([fetchServices(), fetchSystem()]);
  resetCountdown();
}

/* --------------------------------------------------------------------------
   Actions
   -------------------------------------------------------------------------- */

async function doAction(id, action) {
  const btn = document.querySelector(`[data-action="${action}"][data-id="${id}"]`);
  if (btn) btn.classList.add('btn--loading');

  try {
    const result = await api(`/services/${id}/${action}`, 'POST');
    const name = services.find(s => s.id === id)?.name || id;
    if (result.success) {
      showToast(`${capitalize(action)}: ${name}`, 'success');
    } else {
      showToast(`${capitalize(action)} failed: ${result.message || 'Unknown error'}`, 'error');
    }
  } catch {
    showToast(`${capitalize(action)} failed: Connection error`, 'error');
  }

  await new Promise(r => setTimeout(r, 1200));
  await fetchServices();
}

async function doStartAll() {
  try {
    await api('/services/start-all', 'POST');
    showToast('Starting all services...', 'success');
  } catch {
    showToast('Start all failed', 'error');
  }
  await new Promise(r => setTimeout(r, 2000));
  await fetchServices();
}

async function doStopAll() {
  try {
    await api('/services/stop-all', 'POST');
    showToast('Stopping all services...', 'success');
  } catch {
    showToast('Stop all failed', 'error');
  }
  await new Promise(r => setTimeout(r, 1500));
  await fetchServices();
}

/* --------------------------------------------------------------------------
   Render
   -------------------------------------------------------------------------- */

function renderSummary() {
  const el = document.getElementById('status-summary');
  if (!el || !services.length) { if (el) el.innerHTML = ''; return; }

  const total = services.length;
  const running = services.filter(s => s.status === 'running').length;
  const stopped = services.filter(s => s.status === 'stopped').length;
  const starting = services.filter(s => s.status === 'starting').length;
  const degraded = services.filter(s => s.status === 'running' && s.lastCheck && !s.lastCheck.healthy).length;
  const avgLat = services.filter(s => s.avgLatency != null);
  const latency = avgLat.length ? Math.round(avgLat.reduce((a, s) => a + s.avgLatency, 0) / avgLat.length) : null;

  el.innerHTML = `
    <span class="summary__item summary__item--running">
      <span class="summary__dot summary__dot--running"></span>
      ${running} running
    </span>
    ${degraded > 0 ? `
    <span class="summary__item summary__item--degraded">
      <span class="summary__dot summary__dot--degraded"></span>
      ${degraded} degraded
    </span>` : ''}
    ${starting > 0 ? `
    <span class="summary__item summary__item--starting">
      <span class="summary__dot summary__dot--starting"></span>
      ${starting} starting
    </span>` : ''}
    <span class="summary__item summary__item--stopped">
      <span class="summary__dot summary__dot--stopped"></span>
      ${stopped} stopped
    </span>
    ${latency !== null ? `
    <span class="summary__item">
      <span class="summary__label">Avg Latency:</span>
      <span class="summary__value ${latencyClass(latency)}">${latency}ms</span>
    </span>` : ''}
  `;
}

function renderServices() {
  const grid = document.getElementById('services-grid');

  if (!services.length) {
    grid.innerHTML = '<div class="card card--loading"><p>No services configured. Edit services.json to add services.</p></div>';
    return;
  }

  grid.innerHTML = services.map(renderCard).join('');
}

function renderCard(s) {
  const isRunning = s.status === 'running';
  const isStopped = s.status === 'stopped';
  const isStarting = s.status === 'starting';
  const isDegraded = isRunning && s.lastCheck && !s.lastCheck.healthy;
  const cardStatus = isDegraded ? 'degraded' : s.status;

  return `
    <div class="card card--${cardStatus}">
      <div class="card__header">
        <span class="status-dot status-dot--${cardStatus}"></span>
        <div>
          <h3 class="card__name">${esc(s.name)}</h3>
          <span class="card__project">${esc(s.project)}</span>
        </div>
      </div>
      ${s.description ? `<p class="card__desc">${esc(s.description)}</p>` : ''}
      <div class="card__meta">
        <span class="badge badge--${cardStatus}">${isDegraded ? 'Degraded' : capitalize(s.status)}</span>
        ${s.pid ? `<span class="badge badge--pid">PID ${s.pid}</span>` : ''}
        ${s.autoRestart ? '<span class="badge badge--info">Auto-restart</span>' : ''}
        ${s.restartCount > 0 ? `<span class="badge badge--info">Restarts: ${s.restartCount}</span>` : ''}
        ${s.lastCheck && s.lastCheck.reachable ? renderLatencyBadge(s.lastCheck.latency) : ''}
        ${s.healthPercent !== null ? renderHealthBadge(s.healthPercent) : ''}
        ${isDegraded && s.lastCheck?.statusCode ? `<span class="badge badge--degraded">HTTP ${s.lastCheck.statusCode}</span>` : ''}
        ${s.startedAt ? `<span class="badge badge--pid">${formatUptime(s.startedAt)}</span>` : ''}
      </div>
      <div class="card__actions">
        <button class="btn btn--start" data-action="start" data-id="${s.id}"
          onclick="doAction('${s.id}', 'start')" ${isRunning || isStarting ? 'disabled' : ''}>
          ${ICONS.play} Start
        </button>
        <button class="btn btn--stop" data-action="stop" data-id="${s.id}"
          onclick="doAction('${s.id}', 'stop')" ${isStopped ? 'disabled' : ''}>
          ${ICONS.stop} Stop
        </button>
        <button class="btn btn--restart" data-action="restart" data-id="${s.id}"
          onclick="doAction('${s.id}', 'restart')">
          ${ICONS.restart} Restart
        </button>
        ${s.url ? `
          <a class="btn btn--open" href="${esc(s.url)}" target="_blank" rel="noopener">
            ${ICONS.link} Open
          </a>
        ` : ''}
        <button class="btn btn--ghost btn--sm" onclick="openLogModal('${s.id}', '${esc(s.name)}')">
          ${ICONS.logs} Logs
        </button>
        <button class="btn btn--ghost btn--sm" onclick="openEnvModal('${s.id}', '${esc(s.name)}')">
          ${ICONS.settings} Settings
        </button>
      </div>
    </div>
  `;
}

function renderLatencyBadge(ms) {
  return `<span class="badge badge--latency ${latencyClass(ms)}">${ms}ms</span>`;
}

function renderHealthBadge(pct) {
  let cls = 'badge--health-good';
  if (pct < 50) cls = 'badge--health-bad';
  else if (pct < 90) cls = 'badge--health-warn';
  return `<span class="badge ${cls}">${pct}%</span>`;
}

function latencyClass(ms) {
  if (ms <= 100) return 'latency--good';
  if (ms <= 300) return 'latency--warn';
  return 'latency--bad';
}

function renderSystemBar() {
  const bar = document.getElementById('system-bar');
  if (!systemInfo.hostname) return;

  const memUsed = formatBytes(systemInfo.memory?.used || 0);
  const memTotal = formatBytes(systemInfo.memory?.total || 0);

  bar.innerHTML = `
    <span class="system-bar__item">
      <span class="system-bar__label">Host:</span>
      <span class="system-bar__value">${esc(systemInfo.hostname)}</span>
    </span>
    <span class="system-bar__item">
      <span class="system-bar__label">IP:</span>
      <span class="system-bar__value">${esc(systemInfo.localIp)}</span>
    </span>
    <span class="system-bar__item">
      <span class="system-bar__label">Node:</span>
      <span class="system-bar__value">${esc(systemInfo.nodeVersion)}</span>
    </span>
    <span class="system-bar__item">
      <span class="system-bar__label">Memory:</span>
      <span class="system-bar__value">${memUsed} / ${memTotal}</span>
    </span>
    <span class="system-bar__item">
      <span class="system-bar__label">Uptime:</span>
      <span class="system-bar__value">${formatDuration(systemInfo.uptime || 0)}</span>
    </span>
  `;
}

function renderHostInfo() {
  const el = document.getElementById('host-info');
  if (systemInfo.localIp) {
    el.textContent = `${systemInfo.localIp}:3500`;
  }
}

/* --------------------------------------------------------------------------
   Toast
   -------------------------------------------------------------------------- */

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast--${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

/* --------------------------------------------------------------------------
   Helpers
   -------------------------------------------------------------------------- */

function esc(str) {
  const d = document.createElement('div');
  d.textContent = str;
  return d.innerHTML;
}

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function formatBytes(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1048576) return (bytes / 1024).toFixed(0) + ' KB';
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + ' MB';
  return (bytes / 1073741824).toFixed(1) + ' GB';
}

function formatDuration(seconds) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (h > 0) return `${h}h ${m}m`;
  return `${m}m`;
}

function formatUptime(isoDate) {
  const diff = (Date.now() - new Date(isoDate).getTime()) / 1000;
  if (diff < 60) return 'just now';
  return 'up ' + formatDuration(diff);
}

/* --------------------------------------------------------------------------
   Countdown / Auto-refresh
   -------------------------------------------------------------------------- */

function resetCountdown() {
  countdown = REFRESH_INTERVAL / 1000;
  updateCountdown();
}

function updateCountdown() {
  const el = document.getElementById('countdown');
  if (el) el.textContent = countdown + 's';
}

function startAutoRefresh() {
  resetCountdown();

  countdownTimer = setInterval(() => {
    countdown--;
    if (countdown <= 0) countdown = REFRESH_INTERVAL / 1000;
    updateCountdown();
  }, 1000);

  refreshTimer = setInterval(fetchAll, REFRESH_INTERVAL);
}

/* --------------------------------------------------------------------------
   Log Modal
   -------------------------------------------------------------------------- */

let logModalServiceId = null;
let logTailInterval = null;

async function openLogModal(id, name) {
  logModalServiceId = id;
  document.getElementById('log-modal-title').textContent = `Logs — ${name}`;
  document.getElementById('log-modal').classList.add('modal-overlay--open');
  await refreshLogs();
  if (document.getElementById('log-tail').checked) {
    logTailInterval = setInterval(refreshLogs, 3000);
  }
  document.getElementById('log-tail').onchange = (e) => {
    clearInterval(logTailInterval);
    if (e.target.checked) logTailInterval = setInterval(refreshLogs, 3000);
  };
}

function closeLogModal(e) {
  if (e && e.target !== document.getElementById('log-modal')) return;
  document.getElementById('log-modal').classList.remove('modal-overlay--open');
  clearInterval(logTailInterval);
  logTailInterval = null;
  logModalServiceId = null;
}

async function refreshLogs() {
  if (!logModalServiceId) return;
  try {
    const data = await api(`/services/${logModalServiceId}/logs?lines=150`);
    const viewer = document.getElementById('log-viewer');
    if (!data.lines || data.lines.length === 0) {
      viewer.innerHTML = '<span class="log-empty">No log entries yet.</span>';
      return;
    }
    viewer.innerHTML = data.lines.map(l => `<div class="log-line">${escLog(l)}</div>`).join('');
    viewer.scrollTop = viewer.scrollHeight;
  } catch {
    document.getElementById('log-viewer').innerHTML = '<span class="log-empty log-empty--error">Failed to load logs.</span>';
  }
}

function escLog(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/(error|failed|warn)/gi, '<span class="log-highlight log-highlight--$1">$&</span>')
    .replace(/(success|200|running|started)/gi, '<span class="log-highlight log-highlight--ok">$&</span>');
}

/* --------------------------------------------------------------------------
   Env / Settings Modal
   -------------------------------------------------------------------------- */

let envModalServiceId = null;

async function openEnvModal(id, name) {
  envModalServiceId = id;
  document.getElementById('env-modal-title').textContent = `Settings — ${name}`;
  document.getElementById('env-modal').classList.add('modal-overlay--open');

  const body = document.getElementById('env-modal-body');
  body.innerHTML = '<p class="modal__loading">Loading…</p>';

  try {
    const data = await api(`/services/${id}/env`);
    if (!data.envKeys || data.envKeys.length === 0) {
      body.innerHTML = '<p class="modal__loading">No configurable settings for this service.</p>';
      return;
    }
    body.innerHTML = `
      <p class="env-desc">These values are saved to <code>.env</code> (gitignored). Saving will restart the service.</p>
      <div class="env-fields">
        ${data.envKeys.map(k => `
          <div class="env-field">
            <label class="env-label" for="env-${k}">${k}</label>
            <input class="env-input" type="password" id="env-${k}" data-key="${k}"
              placeholder="Enter value…" autocomplete="off">
            <button class="env-show-btn" type="button" onclick="toggleEnvVisibility('env-${k}', this)">Show</button>
          </div>
        `).join('')}
      </div>
    `;
  } catch {
    body.innerHTML = '<p class="modal__loading">Failed to load settings.</p>';
  }
}

function closeEnvModal(e) {
  if (e && e.target !== document.getElementById('env-modal')) return;
  document.getElementById('env-modal').classList.remove('modal-overlay--open');
  envModalServiceId = null;
}

function toggleEnvVisibility(inputId, btn) {
  const input = document.getElementById(inputId);
  if (input.type === 'password') { input.type = 'text'; btn.textContent = 'Hide'; }
  else { input.type = 'password'; btn.textContent = 'Show'; }
}

async function saveEnvAndRestart() {
  if (!envModalServiceId) return;
  const fields = document.querySelectorAll('#env-modal-body .env-input');
  let saved = 0;
  for (const field of fields) {
    const key = field.dataset.key;
    const value = field.value.trim();
    if (!value) continue;
    try {
      const r = await fetch(`/api/services/${envModalServiceId}/env`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ key, value })
      });
      if (r.ok) saved++;
    } catch { /* handled below */ }
  }
  if (saved > 0) {
    showToast(`Saved ${saved} setting(s). Restarting service…`, 'success');
    closeEnvModal();
    await new Promise(r => setTimeout(r, 500));
    await doAction(envModalServiceId, 'restart');
  } else {
    showToast('No changes to save (fields empty?)', 'info');
  }
}

/* --------------------------------------------------------------------------
   Init
   -------------------------------------------------------------------------- */

document.addEventListener('DOMContentLoaded', () => {
  fetchAll();
  startAutoRefresh();
});
