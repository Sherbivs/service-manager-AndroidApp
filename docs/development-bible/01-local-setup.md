# § 1 — Local Setup

## Clone & Install

```bash
git clone <repo-url>
cd service-manager
npm install
```

## Run

```bash
npm start
```

Dashboard: `http://localhost:3500`

## Project Structure

```
server.js        — API server + process management
services.json    — Service configuration
public/          — Dashboard SPA (HTML/CSS/JS)
ops/             — Governance (NEXT pointer, routing)
docs/            — Bible documentation
.github/         — AI instructions
```

## Router Discipline

Every directory has a `ROUTER.md` documenting its purpose and owned files. When adding new directories or changing file purposes, update the relevant `ROUTER.md` files in the same commit.

## Key Development Notes

- **No build step** — Edit files directly, refresh browser
- **No TypeScript** — Vanilla JavaScript throughout
- **Single dependency** — Express only (keep it lightweight)
- **Hot-reload config** — `services.json` changes take effect on next API call without restart
