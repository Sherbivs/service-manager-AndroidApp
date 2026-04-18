# ROUTER
Title: Dashboard Frontend
Purpose: Static SPA serving the Service Manager web dashboard. Vanilla HTML/CSS/JS, no build step.
Owned Globs: public/**
Last Updated (UTC): 2026-04-18T00:00:00Z

Notes:
- No framework, no bundler — files are served directly by Express static middleware.
- Auto-refreshes every 5 seconds via fetch polling.
- Dark theme, responsive grid, toast notifications.

Areas:
- index.html — Dashboard shell (topbar, system bar, service grid, toast container)
- app.js — Client logic (API calls, rendering, auto-refresh, actions)
- style.css — Dark theme styles (CSS custom properties, responsive grid)
