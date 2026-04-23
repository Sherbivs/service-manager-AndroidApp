# Copilot Instructions — Service Manager Android App

## Project Overview

This is a **native Android companion app** for the [Service Manager](https://github.com/Sherbivs/service-manager) dashboard. It connects to the Node.js service manager REST API over LAN and lets users monitor and control services from any Android device.

**Stack:** Kotlin, MVVM, Retrofit + OkHttp, ViewBinding, Material 3, `androidx.security.crypto`  
**Build:** Gradle (no Node.js, no web build step)  
**Min SDK:** 24 (Android 7.0) | **Target SDK:** 34 (Android 14)

## Critical: Start Here Before Any Work

### Orientation Flow (Required Reading)
1. **`Patch.md`** — AI Project Manager Contract. Defines Absolutes, workflow, task discipline.
2. **`Prompt.md`** — Current timestamp, recent changes, PATCHSET echo template.
3. **`Tasklist.md`** — Active backlog with READY queue, dependencies, acceptance criteria.
4. **`ops/NEXT.yaml`** — Current task pointer (must point to READY task).
5. **`AGENTS.md`** — Comprehensive operations guide.

### Router Navigation System
- **`ROUTER.md`** — Root router. Start here to locate any file.
- **`ops/ROUTER.yaml`** — Machine-readable canonical routing map.
- **Every directory has `ROUTER.md`** documenting its purpose and owned files.
- When touching code: update parent + child routers in the same commit.

### Core Control Documents (Never Rename/Delete)
- `Patch.md`, `Tasklist.md`, `Prompt.md` — Meta workflow coordination
- `ROUTER.md`, `ops/ROUTER.yaml` — Routing manifests
- Any `ROUTER.md` file — Subsystem documentation

## Architecture Essentials

### Project Structure
```
app/src/main/
  java/com/servicemanager/app/
    ui/          — Activities, Fragments, ViewModels
    data/        — Retrofit API client, response models, repository
    util/        — EncryptedPrefs helper, extensions
  res/
    layout/      — XML layouts
    values/      — strings, colors, themes
  AndroidManifest.xml
ops/             — Governance (NEXT.yaml, ROUTER.yaml, TOUCHMAP.yaml)
docs/            — Bible documentation
```

### Key Design Principles
- **MVVM** — All UI logic in ViewModels. Activities/Fragments observe StateFlow/LiveData only.
- **Single repository** — `ServiceRepository` is the only entry point to network data.
- **Configurable base URL** — Never hardcode server IP. Read from `EncryptedSharedPreferences`.
- **No coroutines in Activities** — Always use `viewModelScope` for coroutine launches.
- **ViewBinding only** — No `findViewById`, no data binding.

### Service Manager API (what this app calls)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/services` | GET | List all services with live status |
| `/api/services/:id/start` | POST | Start a service |
| `/api/services/:id/stop` | POST | Stop a service |
| `/api/services/:id/restart` | POST | Restart a service |
| `/api/services/:id/logs` | GET | Recent log lines (`?lines=N`) |
| `/api/services/:id/logs/archive` | GET | Search archived logs |
| `/api/system` | GET | System info (hostname, IP, memory, uptime) |

## Security Requirements

### Mandatory Rules
- **Never hardcode** server URLs, API tokens, or credentials in source code or resources.
- **Store secrets** in `EncryptedSharedPreferences` (androidx.security.crypto) or Android Keystore.
- **Network:** HTTPS enforced via `network_security_config.xml`. For LAN HTTP, add an explicit `<domain>` exception — do NOT set `usesCleartextTraffic="true"` globally.
- **Manifest:** `android:exported="false"` on all components not requiring external launch.
- **Release builds:** `minifyEnabled true`, `shrinkResources true`, `debuggable false`.
- **Keystore:** `keystore.properties` and `*.jks`/`*.keystore` are gitignored. Never commit them.
- **Logging:** Strip sensitive data from Logcat in release builds (use BuildConfig.DEBUG guards).

### OWASP MASVS Alignment
- M1 (Improper Platform Usage): Use runtime permissions for dangerous perms; declare minimum perms.
- M2 (Insecure Data Storage): EncryptedSharedPreferences for all sensitive values.
- M3 (Insecure Communication): TLS 1.2+; network_security_config blocks cleartext.
- M9 (Reverse Engineering): R8 obfuscation on release; no sensitive strings in code.

## Development Workflows

### Common Commands
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (requires keystore)
./gradlew installDebug           # Install on connected device
./gradlew lint                   # Run Android Lint
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests
```

### Adding a New Feature
1. Create ViewModel in `ui/` — expose `StateFlow<UiState>`
2. Create Fragment/Activity in `ui/` — observe ViewModel, update views via ViewBinding
3. Add API call in `data/ApiService.kt` (Retrofit interface)
4. Add repository method in `data/ServiceRepository.kt`
5. Update `ROUTER.md` for any new packages/directories
6. Write unit tests for the ViewModel

### Router Discipline
Every code change MUST update routers in same commit:
1. Update the parent directory's `ROUTER.md` if adding files
2. Create `ROUTER.md` for any new subdirectory
3. Update `ops/ROUTER.yaml` if adding major new components

### Commit Messages
```
[Task SMA.XXX] Brief summary

Why: Business/technical reason
Changes: Major modifications
Testing: How verified
```

## Common Pitfalls
1. **LAN HTTP** — The service manager runs plain HTTP. Use a domain exception in `network_security_config.xml`, not global cleartext.
2. **ViewModel scope** — `viewModelScope.launch {}` only. Never `GlobalScope` or `lifecycleScope` from Activity for data ops.
3. **Base URL trailing slash** — Retrofit requires base URL to end with `/`. Ensure this when user saves server URL.
4. **Rotation** — ViewModel survives rotation; don't re-fetch on resume if data is fresh.
5. **Keystore** — `keystore.properties` must never be committed. Document the format in `docs/operations-bible/`.
6. **No TypeScript / Node.js** — This is Android-only. Don't add web/server files.


## Critical: Start Here Before Any Work

### Orientation Flow (Required Reading)
1. **`Patch.md`** — AI Project Manager Contract. Defines Absolutes, workflow, and task discipline.
2. **`Prompt.md`** — Current timestamp, recent changes, PATCHSET echo template.
3. **`Tasklist.md`** — Active backlog with READY queue, dependencies, acceptance criteria.
4. **`ops/NEXT.yaml`** — Current task pointer (must point to READY task).
5. **`AGENTS.md`** — Comprehensive operations guide.

### Router Navigation System
- **`ops/ROUTER.yaml`** — Canonical AI routing map. Start here to locate any file.
- **`masterroutetable.md`** — Human-readable router index.
- **Every directory has `ROUTER.md`** documenting purpose, owned files, and key integration points.
- When touching code: update parent + child routers in same commit.

### Core Control Documents (Never Rename/Delete)
- `Patch.md`, `Tasklist.md`, `Prompt.md` — Meta workflow coordination
- `ops/ROUTER.yaml`, `masterroutetable.md` — Routing manifests
- Any `ROUTER.md` file — Subsystem documentation

## Architecture Essentials

### Project Structure
```
server.js        — Express API server + process registry + health checks + auto-restart
services.json    — Service configuration (port, service definitions)
public/          — Dashboard SPA (index.html, app.js, style.css)
ops/             — Governance (NEXT.yaml, ROUTER.yaml, TOUCHMAP.yaml)
docs/            — Bible documentation (architecture, operations, development)
```

### Key Design Principles
- **Zero unnecessary dependencies** — Express only. Don't add packages without strong justification.
- **No build step** — Vanilla JS throughout. Files served directly.
- **Hot-reload config** — `services.json` re-read on every API request.
- **LAN accessible** — Binds to `0.0.0.0`.
- **Windows-first** — Process management uses `taskkill`. Cross-platform is a future goal.

### API Endpoints
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/services` | GET | List all services with live status |
| `/api/services/:id/start` | POST | Start a service |
| `/api/services/:id/stop` | POST | Stop a service |
| `/api/services/:id/restart` | POST | Restart a service |
| `/api/system` | GET | System info |
| `/api/logs?lines=50` | GET | Recent log lines |

## Development Workflows

### Common Commands
```bash
npm install    # Install dependencies
npm start      # Start the server
```

### Router Discipline
Every code change MUST update routers in same commit:
1. Update parent `ROUTER.md` (e.g., `public/ROUTER.md`)
2. Create child `ROUTER.md` if creating new subdirectory
3. Update `masterroutetable.md` if adding directories

### Commit Messages
```
[Task SM.XXX.YYY] Brief summary

Why: Business/technical reason
Changes: Major modifications
Testing: How verified
```

## Documentation Standards

### Bible System
All knowledge flows into the Bible docs:
- **Architecture Bible** (`docs/architecture-bible/`) — System design, components, API
- **Operations Bible** (`docs/operations-bible/`) — Install, configure, monitor, troubleshoot
- **Development Bible** (`docs/development-bible/`) — Setup, conventions, contributing

Transient notes belong in `Tasklist.md` or `Prompt.md`, NOT as standalone markdown files.

## Common Pitfalls

1. **Don't add dependencies** unless absolutely necessary. This is a lightweight tool.
2. **Router updates** — Forgetting to update `ROUTER.md` files breaks navigation for AI agents.
3. **services.json** — Never overwrite user service entries without explicit permission.
4. **Windows-specific** — `taskkill` is Windows-only. Guard cross-platform changes.
5. **No TypeScript** — Keep it vanilla JS. The zero-build-step design is intentional.

## Quick Reference

- **Architecture:** `docs/architecture-bible/INDEX.md`
- **Operations:** `docs/operations-bible/INDEX.md`
- **Development:** `docs/development-bible/INDEX.md`
- **Task backlog:** `Tasklist.md`
- **Current task:** `ops/NEXT.yaml`
