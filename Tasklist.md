# Tasklist.md — Service Manager Android App
**Last Updated:** 2026-04-22

---

## READY Queue (work in this order)

### SMA.001 — Network Layer Foundation
**Status:** READY  
**Priority:** P0 — Blocks everything  
**Acceptance Criteria:**
- [ ] `data/ApiService.kt` Retrofit interface with all 7 endpoints
- [ ] `data/ServiceRepository.kt` wraps all API calls, returns sealed Result types
- [ ] `data/RetrofitClient.kt` builds OkHttpClient + Retrofit, reads base URL from EncryptedSharedPreferences
- [ ] Base URL validated on set (must end with `/`, must be valid URL)
- [ ] Unit tests: `ServiceRepositoryTest` with mocked Retrofit responses
- [ ] No hardcoded URLs anywhere

---

### SMA.002 — Server URL Onboarding / Settings Screen
**Status:** BLOCKED — depends on SMA.001  
**Priority:** P0 — App is unusable without server URL  
**Acceptance Criteria:**
- [ ] First-run: user prompted for server URL before navigating to main screen
- [ ] Settings screen accessible from main screen (toolbar menu or FAB)
- [ ] URL stored via `util/EncryptedPrefsHelper.kt`
- [ ] Basic URL validation (non-empty, starts with `http://` or `https://`)
- [ ] "Test Connection" button that pings `/api/system` and shows result
- [ ] ROUTER.md updated for new UI files

---

### SMA.003 — Services List Screen
**Status:** BLOCKED — depends on SMA.001  
**Priority:** P1 — Core feature  
**Acceptance Criteria:**
- [ ] `ServicesViewModel` exposes `StateFlow<ServicesUiState>` (loading, success, error)
- [ ] `ServicesFragment` with RecyclerView showing: name, status badge, port, action buttons
- [ ] Pull-to-refresh
- [ ] Auto-refresh every 10 seconds (stops on background, resumes on foreground)
- [ ] Status colors: green = running, red = stopped, yellow = unknown
- [ ] ViewModel survives rotation without re-fetching if data < 10s old

---

### SMA.004 — Service Actions (Start / Stop / Restart)
**Status:** BLOCKED — depends on SMA.003  
**Priority:** P1  
**Acceptance Criteria:**
- [ ] Tapping Start/Stop/Restart fires the correct POST endpoint
- [ ] Button disabled while action in flight (prevents double-tap)
- [ ] Snackbar feedback: success message or error with retry
- [ ] List auto-refreshes after action completes
- [ ] Unit tests: action dispatched correctly, error state handled

---

### SMA.005 — System Info Screen
**Status:** BLOCKED — depends on SMA.001  
**Priority:** P2  
**Acceptance Criteria:**
- [ ] Displays: hostname, IP, Node version, memory used/total, uptime
- [ ] Accessible from bottom nav or overflow menu
- [ ] Refreshes on screen focus

---

### SMA.006 — Log Viewer Screen
**Status:** BLOCKED — depends on SMA.001  
**Priority:** P2  
**Acceptance Criteria:**
- [ ] Displays last N log lines (N configurable, default 100)
- [ ] Archive search: query field → calls `/api/services/:id/logs/archive`
- [ ] Auto-scroll to bottom on new lines
- [ ] "Copy to clipboard" button
- [ ] Long log lines truncate with expand-on-tap

---

### SMA.007 — Network Security Config (LAN HTTP support)
**Status:** READY (no dependencies)  
**Priority:** P0 — Required for app to reach LAN HTTP server  
**Acceptance Criteria:**
- [ ] `res/xml/network_security_config.xml` created
- [ ] Config allows cleartext for user-configured LAN host only (not globally)
- [ ] `AndroidManifest.xml` references `android:networkSecurityConfig`
- [ ] `usesCleartextTraffic="true"` NOT set globally in manifest
- [ ] ROUTER.md updated to document `res/xml/`

---

### SMA.008 — Release Build Hardening
**Status:** BLOCKED — depends on SMA.001, SMA.003, SMA.004  
**Priority:** P1 — Required before any distribution  
**Acceptance Criteria:**
- [ ] `app/build.gradle` release config: `minifyEnabled true`, `shrinkResources true`, `debuggable false`
- [ ] ProGuard/R8 rules: keep Retrofit models, Gson annotations; strip logs in release
- [ ] `keystore.properties` pattern documented in `docs/operations-bible/`
- [ ] Verified: `./gradlew assembleRelease` succeeds with dummy keystore
- [ ] Verified: no sensitive strings visible via `strings` tool on release APK

---

## IN PROGRESS

*(none)*

---

## DONE

*(none)*

---

## BACKLOG (future consideration)

- SMA.100 — Dark mode / theme toggle
- SMA.101 — Widget: service status on home screen
- SMA.102 — Push notifications for service crashes (requires server-side webhook support)
- SMA.103 — Multi-server support (save multiple server URLs)
- SMA.104 — Biometric lock for app access
