# Prompt.md
Timestamp (UTC): 2026-04-18T12:00:00Z
Revision: 1.1

**Cross-References**:
- See `Patch.md` for contract rules
- See `Tasklist.md` for active backlog and current tasks
- See `ops/NEXT.yaml` for current task pointer

## Purpose & Scope
Canonical brief for automation: captures current mission, guardrails, and truth sources so every run starts aligned.

## Activation Checklist
1. Ensure Patch.md, Tasklist.md, Prompt.md, and ops/NEXT.yaml open cleanly.
2. Read ops/NEXT.yaml and confirm the READY queue in Tasklist.md matches.
3. Review the PATCHSET echo for context on outstanding work.
4. If router manifests diverge from working intent, update them before editing.

## Last Repo Changes
- **2026-04-18 (Rev 1.2):** Silent startup via VBS — created `launch-server.vbs` (hidden window style, idempotent check); updated `service-install.js` to register it in Windows Startup folder alongside the tray; updated `service-uninstall.js` to remove both shortcuts. Synced TOUCHMAP, ROUTER.yaml, ROUTER.md, masterroutetable.
- **2026-04-18 (Rev 1.1):** Codespace audit — fixed TOUCHMAP filenames, stale ROUTER.yaml glob, expanded masterroutetable, updated architecture-bible project structure.

## PATCHSET Echo
<<<PATCHSET START>>>
Header: [Meta] Codespace audit — meta-router and docs sync

Summary: Audited workspace against actual disk state. Fixed TOUCHMAP filenames, removed stale .github/instructions/* glob from ROUTER.yaml, expanded masterroutetable Key Files table with 5 missing root files, updated architecture-bible project structure. SM.INIT.AUTO_START scripts confirmed present (service-install.js, service-uninstall.js, tray.ps1, launch-tray.vbs, node-windows dep) — task remains READY pending functional verification.

TasklistDiff: false
Tests: N/A
Contracts: ON
READY pointer: SM.INIT.AUTO_START (verify/test auto-start behavior)
<<<PATCHSET END>>>
