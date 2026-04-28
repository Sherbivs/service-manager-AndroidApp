# Tasklist.md — Service Manager Android App
**Last Updated:** 2026-04-28

---

## READY Queue (work in this order)

### SMA.002 — Server URL Onboarding / Settings Screen
**Status:** DONE
**Priority:** P0 — App is unusable without server URL
**Acceptance Criteria:**
- [x] `util/SecurePrefsHelper.kt` stores URL securely
- [x] Retrofit interceptor handles dynamic URL updates
- [x] First-run: user prompted for server URL before navigating to main screen
- [x] Settings screen accessible from main screen (bottom nav)
- [x] UI to view/edit Server URL
- [x] Basic URL validation (non-empty, starts with `http://` or `https://`)
- [x] "Test Connection" button that pings `/api/system` and shows result
- [x] ROUTER.md updated for new UI files (`ui/settings/ROUTER.md`, `ui/onboarding/ROUTER.md`)

---

### SMA.003 — Services List Screen
**Status:** DONE
**Priority:** P1 — Core feature
**Acceptance Criteria:**
- [x] `data/api/ApiService.kt` Retrofit interface with all 7 endpoints
- [x] `data/repository/ServiceRepository.kt` wraps all API calls, returns sealed Result types
- [x] `data/repository/ServiceRepositoryTest.kt` smoke tests with MockWebServer
- [x] `ServicesViewModel` exposes `StateFlow<ServicesUiState>` (loading, success, error)
- [x] `ServicesFragment` with RecyclerView showing: name, status badge, port, action buttons
- [x] Pull-to-refresh
- [x] Auto-refresh every 10 seconds (stops on background, resumes on foreground)
- [x] Status colors: green = running, red = stopped, yellow = unknown
- [x] ViewModel survives rotation without re-fetching if data < 10s old (`loadServicesIfStale` + `lastFetchTimeMs`)

---

### SMA.004 — Service Actions (Start / Stop / Restart)
**Status:** DONE
**Priority:** P1
**Acceptance Criteria:**
- [x] Tapping Start/Stop/Restart fires the correct POST endpoint
- [x] Button disabled while action in flight (prevents double-tap)
- [x] Snackbar feedback: success message or error with retry
- [x] List auto-refreshes after action completes
- [x] Unit tests: action dispatched correctly, error state handled

---

### SMA.005 — System Info Screen
**Status:** DONE
**Priority:** P2
**Acceptance Criteria:**
- [x] Displays: hostname, IP, Node version, memory used/total, uptime
- [x] Accessible from bottom nav
- [x] Refreshes on screen focus

---

### SMA.006 — Log Viewer Screen
**Status:** DONE
**Priority:** P2
**Acceptance Criteria:**
- [x] Displays last N log lines (N configurable, default 100)
- [x] Archive search: query field → calls `/api/services/:id/logs/archive`
- [x] Auto-scroll to bottom on new lines
- [x] "Copy to clipboard" button
- [x] Long log lines truncate with expand-on-tap

---

### SMA.008 — Release Build Hardening
**Status:** DONE
**Priority:** P1 — Required before any distribution
**Acceptance Criteria:**
- [x] `app/build.gradle` release config: `minifyEnabled true`, `shrinkResources true`, `debuggable false`
- [x] ProGuard/R8 rules: keep Retrofit models, Gson annotations; strip logs in release
- [x] `keystore.properties` pattern documented in `docs/operations-bible/`
- [x] Verified: `./gradlew assembleRelease` succeeds with dummy keystore
- [x] Verified: no sensitive strings visible via `strings` tool on release APK

---

## DONE

### SMA.001 — Network Layer Foundation
### SMA.007 — Network Security Config
### SMA.009 — Dependency Injection (Hilt) Setup
### SMA.010 — Navigation Component Setup
### SMA.011 — Testing Infrastructure
### SMA.012 — Code Quality Tooling (ktlint + detekt)
### SMA.013 — GitHub Actions CI/CD Pipeline
### SMA.014 — QA and Dependency Maintenance (2026-04-24)
### SMA.015 — Android UX/UI + Scalability QA (2026-04-28)
- Added log archive project filter + pagination UX and page state visibility.
- Added archive request cancellation and dialog collector lifecycle cleanup.
- Added debounced/off-main log filtering and readability improvements.
- Added services empty state, clearer error copy, haptic action feedback, and stopped-state clarity.
- Verification: `assembleDebug` successful.

### SMA.016 — Documentation Sync (2026-04-28)
- Updated Prompt.md, Tasklist.md, and ops/NEXT.yaml to reflect completed work.
- Updated development and architecture Bible documentation for API/log UX and product-readiness direction.

---

## BACKLOG (future consideration)

- SMA.100 — Dark mode / theme toggle
- SMA.101 — Widget: service status on home screen
- SMA.102 — Push notifications for service crashes (requires server-side webhook support)
- SMA.103 — Multi-server support (save multiple server URLs)
- SMA.104 — Biometric lock for app access
- SMA.105 — WorkManager: periodic background sync (requires battery impact review)
- SMA.106 — Favorites + pinned services dashboard
- SMA.107 — Batch service actions (multi-select Start/Stop/Restart)
- SMA.108 — Role-aware UI gating (viewer/operator/admin)
