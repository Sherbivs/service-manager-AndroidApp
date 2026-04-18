# § 3 — Dashboard Architecture

## Overview

The dashboard is a vanilla SPA (no framework, no bundler) served as static files from `public/`.

## Components

### index.html
- Semantic layout: topbar → system bar → service grid → toast container
- No external CDN dependencies — fully self-contained

### app.js
- **Polling loop** — Fetches `/api/services` and `/api/system` every 5 seconds
- **Action handlers** — `doAction(id, action)` calls POST endpoints, shows toast, refreshes
- **Render functions** — Template literal-based rendering (no virtual DOM)
- **Connection state** — Green/red dot in topbar based on API reachability

### style.css
- CSS custom properties for theming (dark theme by default)
- Responsive grid: `repeat(auto-fill, minmax(360px, 1fr))`
- Status-aware card styling (running = green border, starting = amber, stopped = default)

## Data Flow

```
DOMContentLoaded
  → fetchAll()
    → GET /api/services → renderServices()
    → GET /api/system   → renderSystemBar() + renderHostInfo()
  → startAutoRefresh() (5s interval)

User clicks Start/Stop/Restart
  → doAction(id, action)
    → POST /api/services/:id/:action
    → showToast(result)
    → 1.2s delay
    → fetchServices() (refresh cards)
```

## Toast System

Toasts appear in bottom-right corner, auto-dismiss after 4 seconds.
Types: `success`, `error`, `info` — each styled differently.
