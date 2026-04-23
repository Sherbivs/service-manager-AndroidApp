# Tasklist.md — Service Manager Android App
**Last Updated:** 2026-04-22

---

## READY Queue (work in this order)

### SMA.001 — Network Layer Foundation
**Status:** READY  
**Priority:** P0 — Blocks everything  
**Acceptance Criteria:**
- [ ] `data/api/ApiService.kt` Retrofit interface with all 7 endpoints
- [ ] `data/repository/ServiceRepository.kt` wraps all API calls, returns `Result<T>` types
- [ ] `data/model/` — DTOs: `ServiceDto.kt`, `SystemInfoDto.kt`, `LogEntryDto.kt`
- [ ] Base URL read from `EncryptedSharedPreferences` via `util/EncryptedPrefsHelper.kt`
- [ ] Base URL validated on set (must end with `/`, must be valid URL)
- [ ] Unit tests: `ServiceRepositoryTest` with `MockWebServer` — test all endpoints, error cases
- [ ] No hardcoded URLs anywhere
- [ ] Hilt wiring complete (see SMA.009 — may be done in parallel)

---

### SMA.007 — Network Security Config (LAN HTTP support)
**Status:** READY (no dependencies)  
**Priority:** P0 — Required for app to reach LAN HTTP server  
**Acceptance Criteria:**
- [ ] `res/xml/network_security_config.xml` created
- [ ] Config allows cleartext for user-configured LAN host via `<domain>` element, not globally
- [ ] `AndroidManifest.xml` references `android:networkSecurityConfig="@xml/network_security_config"`
- [ ] `usesCleartextTraffic="true"` NOT set globally in manifest
- [ ] `app/ROUTER.md` updated to document `res/xml/`

---

### SMA.009 — Dependency Injection (Hilt) Setup
**Status:** READY  
**Priority:** P0 — Required before any feature work  
**Acceptance Criteria:**
- [ ] `com.google.dagger:hilt-android` added to `app/build.gradle` dependencies
- [ ] Hilt Gradle plugin applied in root `build.gradle` and `app/build.gradle`
- [ ] `ServiceManagerApp.kt` created with `@HiltAndroidApp`
- [ ] `AndroidManifest.xml` references `android:name=".ServiceManagerApp"`
- [ ] `di/NetworkModule.kt` created with `@Module @InstallIn(SingletonComponent::class)`
- [ ] `di/AppModule.kt` created for non-network singleton bindings (EncryptedPrefsHelper, etc.)
- [ ] `MainActivity.kt` annotated with `@AndroidEntryPoint`
- [ ] Verified: `./gradlew assembleDebug` succeeds with Hilt wired

---

### SMA.010 — Navigation Component Setup
**Status:** BLOCKED — depends on SMA.009  
**Priority:** P0 — Required for single-activity architecture  
**Acceptance Criteria:**
- [ ] `androidx.navigation:navigation-fragment-ktx` and `navigation-ui-ktx` added
- [ ] Safe Args plugin applied in `build.gradle`
- [ ] `res/navigation/nav_graph.xml` created with placeholder destinations
- [ ] `MainActivity.kt` sets up `NavHostFragment` as the content view
- [ ] `BottomNavigationView` wired to `NavController` for Services, System, Logs
- [ ] `app/ROUTER.md` updated to document `res/navigation/`
- [ ] Verified: navigation between stub screens works on device

---

### SMA.011 — Testing Infrastructure
**Status:** READY  
**Priority:** P1 — Required before writing any feature tests  
**Acceptance Criteria:**
- [ ] `app/build.gradle` test dependencies added:
  - `junit:junit:4.13.2`
  - `io.mockk:mockk:1.13.x`
  - `app.cash.turbine:turbine:x.x`
  - `org.jetbrains.kotlinx:kotlinx-coroutines-test`
  - `com.squareup.okhttp3:mockwebserver`
- [ ] `app/build.gradle` androidTest dependencies:
  - `androidx.test.espresso:espresso-core`
  - `androidx.fragment:fragment-testing`
  - `com.google.dagger:hilt-android-testing`
- [ ] `MainCoroutineRule.kt` utility created in `src/test/` (provides `UnconfinedTestDispatcher`)
- [ ] Smoke test: `ServicesViewModelTest.kt` with one passing test as proof-of-concept
- [ ] `./gradlew test` passes with no errors

---

### SMA.012 — Code Quality Tooling (ktlint + detekt)
**Status:** READY  
**Priority:** P1 — Required before any code review or merge  
**Acceptance Criteria:**
- [ ] `ktlint` Gradle plugin applied (`org.jlleitschuh.gradle.ktlint`)
- [ ] `detekt` Gradle plugin applied (`io.gitlab.arturbosch.detekt`)
- [ ] `detekt.yml` config file created at root with sensible thresholds (max complexity 20, max function length 40)
- [ ] `.editorconfig` created specifying Kotlin style rules compatible with ktlint
- [ ] `./gradlew ktlintCheck` passes on current codebase
- [ ] `./gradlew detekt` passes on current codebase with zero ERRORs
- [ ] `./gradlew lint` configured with `abortOnError true`
- [ ] CI gate: all three tools run on PR (see SMA.013)

---

### SMA.002 — Server URL Onboarding / Settings Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P0 — App is unusable without server URL  
**Acceptance Criteria:**
- [ ] First-run: `OnboardingFragment` shown if no URL is configured
- [ ] Settings: `SettingsFragment` accessible from main nav
- [ ] URL stored via `util/EncryptedPrefsHelper.kt`
- [ ] Basic URL validation (non-empty, starts with `http://` or `https://`, ends with `/`)
- [ ] "Test Connection" button that pings `/api/system` and shows success/error Snackbar
- [ ] `ServicesViewModelTest` updated to cover URL validation edge cases
- [ ] `ROUTER.md` updated for `ui/settings/` and `ui/onboarding/`

---

### SMA.003 — Services List Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P1 — Core feature  
**Acceptance Criteria:**
- [ ] `ServicesUiState` sealed class: `Loading`, `Success(List<ServiceModel>)`, `Error(String)`
- [ ] `ServicesViewModel` (`@HiltViewModel`) exposes `StateFlow<ServicesUiState>`
- [ ] `ServicesFragment` (`@AndroidEntryPoint`) with RecyclerView: name, status badge, port, action buttons
- [ ] Pull-to-refresh via `SwipeRefreshLayout`
- [ ] Auto-refresh every 10 seconds — pauses on `STOPPED` lifecycle, resumes on `STARTED`
- [ ] Status colours: green = running, red = stopped, amber = unknown/error
- [ ] ViewModel does not re-fetch if cached data is < 10s old
- [ ] Unit test: `ServicesViewModelTest` — Loading → Success, Loading → Error, cache hit
- [ ] UI test: `ServicesFragmentTest` — RecyclerView shows items, error state shown

---

### SMA.004 — Service Actions (Start / Stop / Restart)
**Status:** BLOCKED — depends on SMA.003  
**Priority:** P1  
**Acceptance Criteria:**
- [ ] `ServicesViewModel.startService(id)`, `stopService(id)`, `restartService(id)` methods
- [ ] Buttons disabled while action in-flight (prevent double-tap)
- [ ] Snackbar feedback: success message or error with Retry action
- [ ] List refreshes after action completes (re-fetches immediately)
- [ ] Unit test: action fires correct repo method, error state handled correctly
- [ ] UI test: button click triggers correct action observable in ViewModel

---

### SMA.005 — System Info Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P2  
**Acceptance Criteria:**
- [ ] `SystemUiState` sealed class with `Loading`, `Success(SystemModel)`, `Error`
- [ ] `SystemViewModel` exposes `StateFlow<SystemUiState>`
- [ ] `SystemFragment` displays: hostname, IP, Node version, memory used/total, uptime
- [ ] Refreshes on screen focus (`onResume`)
- [ ] Unit test: `SystemViewModelTest`

---

### SMA.006 — Log Viewer Screen
**Status:** BLOCKED — depends on SMA.001, SMA.009, SMA.010  
**Priority:** P2  
**Acceptance Criteria:**
- [ ] `LogsUiState` with `Loading`, `Success(List<String>)`, `Error`
- [ ] `LogsViewModel` with line-count parameter (default 100)
- [ ] `LogsFragment` with scrollable log list; auto-scrolls to bottom on new data
- [ ] Archive search: query field → calls `/api/services/:id/logs/archive`
- [ ] "Copy to clipboard" button copies all visible log lines
- [ ] Unit test: `LogsViewModelTest`

---

### SMA.008 — Release Build Hardening
**Status:** BLOCKED — depends on SMA.001, SMA.003, SMA.004  
**Priority:** P1 — Required before any distribution  
**Acceptance Criteria:**
- [ ] `app/build.gradle` release: `minifyEnabled true`, `shrinkResources true`, `debuggable false`
- [ ] `proguard-rules.pro` rules: keep Retrofit model classes, Gson annotations, Hilt generated code
- [ ] `BuildConfig.DEBUG` guards on all `Log.*` calls
- [ ] `keystore.properties` pattern documented in `docs/operations-bible/02-release-signing.md`
- [ ] Verified: `./gradlew assembleRelease` succeeds with a dummy keystore
- [ ] Verified: `strings` tool on release APK reveals no server URL, no credentials

---

### SMA.013 — GitHub Actions CI/CD Pipeline
**Status:** READY  
**Priority:** P2 — Ship after core features are testable  
**Acceptance Criteria:**
- [ ] `.github/workflows/ci.yml` created
- [ ] PR trigger: `./gradlew lint ktlintCheck detekt test`
- [ ] Push to `main` trigger: `./gradlew assembleDebug` + upload APK artifact
- [ ] Secrets wired: no hardcoded paths or credentials
- [ ] Workflow passes on clean run with current codebase
- [ ] `ROUTER.md` updated for `.github/workflows/`

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
- SMA.105 — WorkManager: periodic background sync (requires battery impact review)

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
