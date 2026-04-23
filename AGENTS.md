# Service Manager Android App — Agent Operations Guide

## Mission
Build a secure, native Android companion app for the Service Manager dashboard. The app connects to the Node.js service manager API over LAN, displaying service status and allowing start/stop/restart actions from any Android device.

**Cross-References**:
- See `Patch.md` for contract rules, Absolutes, workflow (MUST READ FIRST)
- See `ops/NEXT.yaml` for task queue (up to 3 main + 1 quick task)
- See `Tasklist.md` for READY queue, dependencies, acceptance criteria
- See `Prompt.md` for current state, PATCHSET echo, recent changes
- See `README.md` for getting started guide
- **Service Manager API:** `http://<host>:3500/api/` — see [Sherbivs/service-manager](https://github.com/Sherbivs/service-manager)

## Repo Map
```
app/                    — Android application module
  src/main/
    AndroidManifest.xml — Permissions, activities, network config reference
    java/com/servicemanager/app/
      MainActivity.kt   — Entry point (to be created)
      ui/               — ViewModels, Fragments, Activities
      data/             — API client, models, repository
      util/             — Shared helpers
    res/                — Layouts, drawables, strings, themes
build.gradle            — Root Gradle config (plugin versions)
app/build.gradle        — App module config, signing, dependencies
ops/                    — Governance surface (NEXT pointer, routing)
docs/                   — Bible documentation
.github/                — Copilot/AI agent instructions
```

## Orientation Flow
1. **START HERE:** Read `Patch.md` completely before ANY work.
2. Review `ops/NEXT.yaml` to see active task queue.
3. Review `Prompt.md` for current state and recent changes.
4. Use `ROUTER.md` alongside `ops/ROUTER.yaml` to navigate the project.
5. Consult `README.md` and `docs/` for architecture and operations context.
6. `Tasklist.md` captures backlog intent; only act after verifying readiness.

## How to Build & Run
- **Open in:** Android Studio (Hedgehog 2023.1.1+) or IntelliJ IDEA
- **Build:** `./gradlew assembleDebug`
- **Run on device/emulator:** Android Studio → Run, or `./gradlew installDebug`
- **API min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Language:** Kotlin
- **Requires:** Service Manager server running on LAN at `http://<host>:3500`

## Architecture Overview

```
Android App (Kotlin, MVVM)
  ↕ Retrofit / OkHttp (HTTPS)
Service Manager API (Node.js, port 3500)
  ↕
Managed Service Processes
```

### Key Components
- **UI Layer** — Activities / Fragments + ViewBinding; Material 3 design
- **ViewModel Layer** — StateFlow/LiveData; survives rotation
- **Repository** — Single source of truth; wraps API client
- **API Client** — Retrofit + OkHttp; configurable base URL (stored in EncryptedSharedPreferences)
- **EncryptedPrefs** — Stores server URL and any credentials securely via `androidx.security.crypto`

### Service Manager API Quick Reference
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/services` | GET | List all services with live status |
| `/api/services/:id/start` | POST | Start a service |
| `/api/services/:id/stop` | POST | Stop a service |
| `/api/services/:id/restart` | POST | Restart a service |
| `/api/services/:id/logs` | GET | Recent log lines (`?lines=N`) |
| `/api/services/:id/logs/archive` | GET | Search archived logs |
| `/api/system` | GET | System info (hostname, IP, memory, uptime) |

## Capabilities & Routing Hints
- **planner:** Break down goals into measurable tasks. Use Patch.md workflow.
- **researcher:** Search, synthesize, attach citations.
- **implementer:** Write/change code and config; run tests. MUST follow Patch.md workflow.
- **reviewer:** Verify diffs, run safety checklists; approve or request changes.

## Contract Workflow Quickstart
1. Open `Patch.md` and review the task queue system.
2. Check `ops/NEXT.yaml` for available task slots.
3. Plan file operations following Patch.md guidelines.
4. Execute edits. Follow Kotlin idioms, MVVM patterns, and security invariants below.
5. Update `Prompt.md` and `ops/NEXT.yaml` before finishing.
6. Leave the repository in a consistent state (routers valid, metadata synced).

## Router Contract (Mandatory)
- Every change MUST be accompanied by routing updates when adding files, renaming directories, or changing file purposes.
- Update: affected `ROUTER.md` (parent and child), `masterroutetable.md`, and `ops/ROUTER.yaml` as needed.
- Do not consider a task complete until routers reflect the changes.

## Safety Invariants
- Never exfiltrate secrets in outputs.
- Never hardcode API keys, tokens, or server URLs in source code — use `EncryptedSharedPreferences`.
- `android:debuggable` must be `false` in release builds.
- Never commit `keystore.properties`, `*.jks`, or `*.keystore` files.
- All network traffic must use HTTPS (TLS 1.2+) — no `usesCleartextTraffic` except for LAN development builds.
- Request only declared permissions; never request more than needed.
- On error, self-forgive: reset working memory, keep the log, try a smaller step.
- Keep every change reversible and minimal.

## Android Security Baseline (OWASP MASVS)
- **Secrets:** Store in Android Keystore or `EncryptedSharedPreferences`, never in plaintext prefs, source, or logs.
- **Network:** HTTPS only. Configure `network_security_config.xml`. For LAN-only use, trust user-added CAs explicitly rather than enabling cleartext globally.
- **Permissions:** Declare only required permissions in manifest; use runtime permission flow for dangerous permissions.
- **Manifest:** `android:exported="false"` on all components not intended for external launch.
- **Build:** `minifyEnabled true` + `shrinkResources true` on release builds. R8/ProGuard obfuscation enabled.
- **Signing:** Release keystore managed via `keystore.properties` (gitignored) or CI secrets — never committed.
- **Logging:** No sensitive data (URLs with tokens, credentials, PII) in Logcat in release builds.

## Stuckness Detection & Recovery
A run is **stuck** if:
- No new artifact or state change in 3 consecutive steps
- Same error seen ≥2 times in 10 minutes
- Budget exceeded without milestone progress

Recovery (in order):
1. Shrink the step (narrow scope)
2. Swap strategy (different approach)
3. Reset context (clear ephemeral state)
4. Escalate (document blocker, ask human)

## Documentation Standards
- All knowledge flows into the Bible system:
  - `docs/architecture-bible/` — System design, components, API integration
  - `docs/operations-bible/` — Build, sign, deploy, Play Store
  - `docs/development-bible/` — Setup, coding conventions, contributing
- Transient notes go in `Tasklist.md` or `Prompt.md`, NOT standalone docs.
- Every directory has a `ROUTER.md`.

## Common Pitfalls
1. **LAN HTTP** — The service manager runs plain HTTP on LAN. Use `network_security_config.xml` with a `<domain>` exception for the LAN host rather than enabling cleartext globally.
2. **ViewModel scope** — Don't launch coroutines from Activity; always use `viewModelScope`.
3. **Base URL** — The server IP is user-configurable; never hardcode `192.168.x.x`. Read from `EncryptedSharedPreferences`.
4. **Rotation** — Use `StateFlow`/`LiveData` so UI survives config changes without redundant API calls.
5. **Release signing** — `keystore.properties` is gitignored. CI must inject it via secrets.

## Checklist Before Exit
- `Prompt.md` updated with accurate summary and NEXT pointer status.
- `ops/NEXT.yaml` task queue reflects current work state.
- `Tasklist.md` updated with task status changes.
- Routers synchronized if structural changes made.
- Commit message references task ID and change summary.

---
**Document Version:** 1.0 (2026-04-22) — Initial Android bootstrap
**Last Updated:** 2026-04-22T00:00:00Z
  - `docs/operations-bible/` — Install, configure, monitor, troubleshoot
  - `docs/development-bible/` — Setup, conventions, contributing
- Transient notes go in `Tasklist.md` or `Prompt.md`, NOT standalone docs.
- Every directory has a `ROUTER.md`.

## Common Pitfalls
1. **Don't add dependencies** — Express only unless strongly justified.
2. **Router updates** — Forgetting breaks AI navigation.
3. **services.json** — User configuration, never overwrite without permission.
4. **Windows-specific** — `taskkill` is Windows-only.
5. **No TypeScript** — Vanilla JS, no build step.

## Checklist Before Exit
- `Prompt.md` updated with accurate summary and NEXT pointer status.
- `ops/NEXT.yaml` task queue reflects current work state.
- `Tasklist.md` updated with task status changes.
- Routers synchronized if structural changes made.
- Commit message references task ID and change summary.

## Done Criteria (Per Task)
- Changes implemented and verified.
- Knowledge updated (`docs/` + this guide if behavior changed).
- Routers updated if structural changes.
- Prompt.md PATCHSET echo current.

---

**Document Version:** 1.0 (2026-04-18) — Initial bootstrap
**Last Updated:** 2026-04-18T00:00:00Z
