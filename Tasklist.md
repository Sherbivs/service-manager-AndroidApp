# Tasklist.md — Service Manager Android App
**Last Updated:** 2026-04-22

---

## READY Queue (work in this order)

### SMA.001 — Network Layer Foundation
**Status:** READY  
**Priority:** P0 — Blocks everything  
**Acceptance Criteria:**
- [ ] `data/ApiService.kt` Retrofit interface with all 7 endpoints
- [ ] `data/ServiceRepository.kt` wraps all API calls, returns `sealed Result<T>` (Success/Error)
- [ ] `data/RetrofitClient.kt` builds OkHttpClient + Retrofit; base URL read from `EncryptedSharedPreferences`
- [ ] Base URL normalized on set (must end with `/`, must be valid URL)
- [ ] No hardcoded URLs anywhere in source
- [ ] **Tests:** `ServiceRepositoryTest` with MockK mocks — success path, error path, empty list
- [ ] **Tests:** `RetrofitClientTest` with MockWebServer — verifies URL construction, correct headers, JSON parsing for each endpoint
- [ ] **Tests:** Base URL trailing-slash normalization tested
- [ ] `./gradlew lint test` passes

---

### SMA.002 — Server URL Onboarding / Settings Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009  
**Priority:** P0 — App is unusable without server URL  
**Acceptance Criteria:**
- [ ] First-run: user prompted for server URL before navigating to main screen
- [ ] Settings screen accessible from main screen (toolbar menu)
- [ ] URL stored via `util/EncryptedPrefsHelper.kt`
- [ ] Basic URL validation (non-empty, starts with `http://` or `https://`, normalized trailing slash)
- [ ] "Test Connection" button pings `/api/system` and shows success/failure Snackbar
- [ ] `OnboardingViewModel` injected via Hilt `@HiltViewModel`
- [ ] ROUTER.md updated for new UI files
- [ ] **Tests:** `OnboardingViewModelTest` — URL validation logic, success + error states via Turbine
- [ ] **UI Test:** Onboarding screen launches on first run (Espresso smoke test)

---

### SMA.003 — Services List Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P1 — Core feature  
**Acceptance Criteria:**
- [ ] `ServicesViewModel` exposes `StateFlow<ServicesUiState>` (sealed Loading/Success/Error)
- [ ] UDF enforced: ViewModel never called from RecyclerView adapter; events bubble up through Fragment
- [ ] `ServicesFragment` with RecyclerView: name, status badge, port, action buttons (Start/Stop/Restart)
- [ ] Pull-to-refresh via `SwipeRefreshLayout`
- [ ] Auto-refresh every 10 seconds using `viewModelScope` coroutine (pauses when Fragment not resumed)
- [ ] Status colors: green = running, red = stopped, amber = starting/unknown
- [ ] ViewModel does not re-fetch if data is fresher than 10 seconds (rotation-safe)
- [ ] `@HiltViewModel` injection for `ServicesViewModel`
- [ ] Navigation to Service Detail via Safe Args
- [ ] **Tests:** `ServicesViewModelTest` — loading/success/error state sequence (Turbine), auto-refresh tick, stale-data guard
- [ ] **UI Test:** Services screen launches, RecyclerView shows at least one item (Espresso smoke)

---

### SMA.004 — Service Actions (Start / Stop / Restart)
**Status:** BLOCKED — depends on SMA.003  
**Priority:** P1  
**Acceptance Criteria:**
- [ ] Tapping Start/Stop/Restart fires the correct POST endpoint via Repository
- [ ] ViewModel emits an `ActionState` (sealed: Idle/InFlight/Done/Error) separate from list `UiState`
- [ ] Action buttons disabled while `ActionState` is `InFlight` (prevents double-tap)
- [ ] Snackbar feedback: success message or error with Retry action
- [ ] Services list auto-refreshes after action completes
- [ ] **Tests:** `ServicesViewModelTest` — action dispatched correctly, `ActionState` sequence, error handling, retry logic (Turbine + MockK)

---

### SMA.005 — System Info Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P2  
**Acceptance Criteria:**
- [ ] Displays: hostname, IP, Node version, memory used/total, uptime
- [ ] Accessible from bottom navigation or overflow menu
- [ ] Refreshes on screen focus (lifecycle-aware)
- [ ] `@HiltViewModel` injection
- [ ] **Tests:** `SystemViewModelTest` — success + error states (Turbine)

---

### SMA.006 — Log Viewer Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P2  
**Acceptance Criteria:**
- [ ] Displays last N log lines (N configurable, default 100)
- [ ] Archive search: query field → calls `/api/services/:id/logs/archive`
- [ ] Auto-scroll to bottom on new lines
- [ ] "Copy to clipboard" button
- [ ] Long log lines truncate with expand-on-tap
- [ ] `@HiltViewModel` injection
- [ ] **Tests:** `LogViewerViewModelTest` — log fetch, search, empty state (Turbine)

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
- [ ] ProGuard/R8 rules: keep Retrofit/Gson model classes; suppress logs in release (`-assumenosideeffects Log.*`)
- [ ] `keystore.properties` pattern documented in `docs/operations-bible/02-release-signing.md`
- [ ] Verified: `./gradlew assembleRelease` succeeds with dummy keystore
- [ ] Verified: `strings` on release APK shows no server URLs, no credentials

---

### SMA.009 — Hilt Dependency Injection Setup
**Status:** READY (no dependencies)  
**Priority:** P0 — Blocks SMA.002, SMA.003, SMA.005, SMA.006  
**Acceptance Criteria:**
- [ ] `hilt-android` + `hilt-compiler` added to `app/build.gradle`
- [ ] `@HiltAndroidApp` annotation on `Application` class (`ServiceManagerApp.kt`)
- [ ] `@AndroidEntryPoint` on `MainActivity`
- [ ] `NetworkModule.kt` — Hilt module providing `OkHttpClient`, `Retrofit`, `ApiService`
- [ ] `DataModule.kt` — Hilt module providing `ServiceRepository`, `EncryptedPrefsHelper`
- [ ] `RetrofitClient` removed or replaced by Hilt-provided instances (no manual `new`)
- [ ] **Tests:** Verify `ServiceRepository` can be injected into a test using `@HiltAndroidTest` or constructor injection
- [ ] `./gradlew assembleDebug` passes with Hilt enabled

---

### SMA.010 — Jetpack Navigation Component
**Status:** READY (no dependencies)  
**Priority:** P0 — Blocks SMA.002, SMA.003, SMA.005, SMA.006  
**Acceptance Criteria:**
- [ ] `navigation-fragment-ktx` + `navigation-ui-ktx` + `navigation-safe-args-gradle-plugin` added
- [ ] `res/navigation/nav_graph.xml` defines all destinations: Onboarding, ServicesList, LogViewer, SystemInfo
- [ ] `MainActivity` hosts a single `NavHostFragment`; no other activities for navigation
- [ ] Safe Args plugin generates typed direction classes for all navigations with arguments
- [ ] Bottom navigation (if used) wired to `NavController` via `NavigationUI.setupWithNavController()`
- [ ] Up/Back buttons handled by NavController — no manual `onBackPressed()` override
- [ ] **Tests:** Each destination navigable via `NavController.navigate()` in test using `TestNavHostController`

---

### SMA.011 — Code Quality Toolchain & CI
**Status:** READY (no dependencies)  
**Priority:** P1 — Gates all future PRs  
**Acceptance Criteria:**
- [ ] **ktlint** configured via Gradle plugin; `.editorconfig` in root
- [ ] **Detekt** configured via Gradle plugin; `detekt.yml` baseline in root
- [ ] **Android Lint** `lint.xml` with `abortOnError true` for release builds
- [ ] `./gradlew lint ktlintCheck detekt test` all pass on clean checkout
- [ ] **GitHub Actions** workflow `.github/workflows/ci.yml`:
  - Triggers on push + PR to `main`
  - Steps: checkout → JDK 17 setup → Gradle cache → `lint` → `ktlintCheck` → `detekt` → `test`
  - Fails PR if any step fails
- [ ] ROUTER.md updated for `.github/workflows/`

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
- SMA.105 — Domain/Use Case layer (extract use cases from ViewModel if complexity grows)
- SMA.106 — Gradle modularization (`:core`, `:data`, `:features:services`)
- SMA.107 — Compose UI migration (optional; evaluate after MVVM + Views is stable)
