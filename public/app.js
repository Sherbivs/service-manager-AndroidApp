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
  link:    '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 01-2 2H5a2 2 0 01-2-2V8a2 2 0 012-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>'
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

  // Brief delay then refresh
  await new Promise(r => setTimeout(r, 1200));
  await fetchServices();
}

/* --------------------------------------------------------------------------
   Render
   -------------------------------------------------------------------------- */

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

  return `
    <div class="card card--${s.status}">
      <div class="card__header">
        <span class="status-dot status-dot--${s.status}"></span>
        <div>
          <h3 class="card__name">${esc(s.name)}</h3>
          <span class="card__project">${esc(s.project)}</span>
        </div>
      </div>
      ${s.description ? `<p class="card__desc">${esc(s.description)}</p>` : ''}
      <div class="card__meta">
        <span class="badge badge--${s.status}">${capitalize(s.status)}</span>
        ${s.pid ? `<span class="badge badge--pid">PID ${s.pid}</span>` : ''}
        ${s.autoRestart ? '<span class="badge badge--info">Auto-restart</span>' : ''}
        ${s.restartCount > 0 ? `<span class="badge badge--info">Restarts: ${s.restartCount}</span>` : ''}
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
      </div>
    </div>
  `;
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
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + ' GB';
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
   Init
   -------------------------------------------------------------------------- */

document.addEventListener('DOMContentLoaded', () => {
  fetchAll();
  startAutoRefresh();
});
