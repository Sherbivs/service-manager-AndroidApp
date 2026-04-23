# Service Manager Android App — Agent Operations Guide

## Mission
Build a secure, native Android companion app for the Service Manager dashboard. The app connects to the Node.js service manager API over LAN, displaying service status and allowing start/stop/restart actions from any Android device.

**Cross-References**:
- See `Patch.md` for contract rules, Absolutes, workflow (MUST READ FIRST)
- See `ops/NEXT.yaml` for task queue (up to 3 main + 1 quick task)
- See `Tasklist.md` for READY queue, dependencies, acceptance criteria
- See `Prompt.md` for current state, PATCHSET echo, recent changes
- See `README.md` for getting started guide
- **Service Manager API:** `http://192.168.23.83:3500/api/` — see [Sherbivs/service-manager](https://github.com/Sherbivs/service-manager)
- **Shopify Dev Service (TCB Party Rental):** `http://192.168.23.83:9292`

## Repo Map
```
app/                    — Android application module
  src/main/
    AndroidManifest.xml — Permissions, activities, network config reference
    java/com/servicemanager/app/
      MainActivity.kt   — Single activity; hosts NavHostFragment
      di/               — Hilt modules (NetworkModule, AppModule)
      ui/
        services/       — ServicesFragment, ServicesViewModel
        system/         — SystemFragment, SystemViewModel
        logs/           — LogsFragment, LogsViewModel
        settings/       — SettingsFragment, SettingsViewModel
      data/
        api/            — ApiService.kt (Retrofit interface)
        model/          — DTOs (ServiceDto, SystemInfoDto, etc.)
        repository/     — ServiceRepository.kt
      domain/           — Use Cases (optional; add when logic is shared/complex)
      util/             — EncryptedPrefsHelper, Extensions
    res/
      navigation/       — nav_graph.xml (Navigation Component)
      xml/              — network_security_config.xml
build.gradle            — Root Gradle config (plugin versions, Hilt classpath)
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
- **Target SDK:** 36 (Android 16)
- **Language:** Kotlin
- **Requires:** Service Manager server running on LAN at `http://192.168.23.83:3500`

## Architecture Overview

```
Android App (Kotlin, MVVM + UDF)
  ↕ Retrofit / OkHttp
Service Manager API (Node.js, port 3500)
  ↕
Managed Service Processes
```

### Layered Architecture (Official Android / Sherbivs Standard)

```
UI Layer          — Activities, Fragments, ViewBinding, Material 3
     ↕  StateFlow<UiState> (down)  /  user events (up)
ViewModel Layer   — State holder, coroutine launches, UDF orchestration
     ↕  sealed Result<T>
[Domain Layer]    — Use Cases (optional; add only for complex reusable logic)
     ↕  DTOs
Data Layer        — ServiceRepository → ApiService (Retrofit) + EncryptedPrefs
```

### Unidirectional Data Flow (UDF) — Required Pattern
State flows **down** from ViewModel to UI; events flow **up** from UI to ViewModel. The ViewModel holds a single `StateFlow<UiState>` and never exposes mutable state directly.

```kotlin
// Standard UiState shape (sealed class per screen)
sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<ServiceModel>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

// ViewModel exposes immutable StateFlow
class ServicesViewModel @HiltViewModel constructor(
    private val repo: ServiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServicesUiState>(Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()
}

// Fragment collects with lifecycle awareness
viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { state -> render(state) }
    }
}
```

### Dependency Injection — Hilt (Required)
Use **Hilt** (official Android DI library) for all dependency wiring. Never construct repositories or API clients manually inside ViewModels or Activities.

```kotlin
@HiltAndroidApp class ServiceManagerApp : Application()

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
  fun provideRetrofit(prefs: EncryptedPrefsHelper): Retrofit =
    Retrofit.Builder().build()
    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create()
}

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repo: ServiceRepository
) : ViewModel()
```

### Navigation — Jetpack Navigation Component (Required)
Single-activity architecture with `NavHostFragment`. Each screen is a Fragment. Navigation declared in `res/navigation/nav_graph.xml`. Use Safe Args for type-safe argument passing. No `startActivity()` for in-app navigation.

### Key Components
- **UI Layer** — Fragments + ViewBinding; Material 3; collects StateFlow via `repeatOnLifecycle`
- **ViewModel Layer** — `@HiltViewModel`; exposes `StateFlow<UiState>`; no `Context` access
- **Repository** — `@Singleton`; wraps `ApiService`; returns `Result<T>` sealed types
- **API Client** — Retrofit + OkHttp via Hilt `NetworkModule`; base URL from `EncryptedSharedPreferences`
- **DI Modules** — `di/NetworkModule.kt`, `di/AppModule.kt` wired via Hilt
- **EncryptedPrefs** — `util/EncryptedPrefsHelper.kt`; stores server URL via `androidx.security.crypto`

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

## Testing Standards (Required)

Every feature task must include tests before it can be marked DONE.

### Testing Pyramid
```
         [UI Tests]          — Espresso / FragmentScenario (fewest, slowest)
      [Integration Tests]    — MockWebServer + Repository (medium)
   [Unit Tests]              — JUnit + MockK + Turbine (most, fastest)
```

### Unit Tests (`app/src/test/`)
- **Scope:** ViewModel, Repository, Use Cases, utility classes
- **Libraries:** JUnit 4/5, MockK (Kotlin-native mocks), `kotlinx-coroutines-test`, Turbine (for Flow)
- **Pattern:** Each ViewModel has a corresponding `*ViewModelTest.kt`; each Repository has `*RepositoryTest.kt`
- **Coverage target:** ≥80% line coverage on ViewModel and Repository classes
- **Setup required:** `TestCoroutineDispatcher` / `UnconfinedTestDispatcher` in test rules

```kotlin
@Test fun `fetch services emits Success state`() = runTest {
    val repo = mockk<ServiceRepository> { coEvery { getServices() } returns Result.success(fakeList) }
    val vm = ServicesViewModel(repo)
    vm.uiState.test {
        awaitItem() shouldBe ServicesUiState.Loading
        vm.loadServices()
        awaitItem() shouldBe ServicesUiState.Success(fakeList)
    }
}
```

### Integration Tests (`app/src/test/` with OkHttp MockWebServer)
- Use `MockWebServer` to simulate the Service Manager API
- Verify `ServiceRepository` parses responses and handles HTTP errors correctly
- Run as JVM tests (no device needed)

### UI / Instrumented Tests (`app/src/androidTest/`)
- Use `FragmentScenario` to launch individual Fragments in isolation
- Use `Espresso` for view interactions and assertions
- Only test user-visible behaviour — not implementation details

## Code Quality Standards (Required)

All PRs and task completions must pass quality gates before merging.

### Tools
| Tool | Purpose | Command |
|------|---------|---------|
| Android Lint | Resource, manifest, API-level issues | `./gradlew lint` |
| ktlint | Kotlin code style enforcement | `./gradlew ktlintCheck` |
| detekt | Kotlin static analysis (complexity, smell detection) | `./gradlew detekt` |

### Rules
- **No lint errors** — `lintOptions { abortOnError true }` enforced in CI
- **ktlint** — Format before commit: `./gradlew ktlintFormat`
- **detekt** — Max complexity threshold enforced; any `detekt` ERROR is a blocker
- **Zero `TODO` comments** in merged code — use Tasklist.md instead
- **No `@SuppressWarnings` / `@Suppress` without a comment** explaining why

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
  - `docs/architecture-bible/` — System design, MVVM+UDF+DI patterns, API integration
  - `docs/operations-bible/` — Build, sign, release, keystore setup, Play Store deployment
  - `docs/development-bible/` — Setup, coding conventions, testing, contributing
- Audit and QA artifacts flow into the archive system under `docs/archive/tasks/`.
- Transient notes go in `Tasklist.md` or `Prompt.md`, NOT standalone docs.
- Every directory has a `ROUTER.md`.

## Audit System Rules (Mandatory)

These rules define the required audit workflow for this repository.

### Canonical Audit Paths
- Parent archive router: `docs/archive/ROUTER.md`
- Task/audit parent router: `docs/archive/tasks/ROUTER.md`
- Audit lifecycle index: `docs/archive/tasks/README.md`
- Stage directories:
  - `docs/archive/tasks/new/`
  - `docs/archive/tasks/in-progress/`
  - `docs/archive/tasks/closed/`
  - `docs/archive/tasks/canceled/`

### Lifecycle Rules
1. Create each new audit report in `docs/archive/tasks/new/`.
2. Move report to `docs/archive/tasks/in-progress/` when remediation starts.
3. Move report to `docs/archive/tasks/closed/` when verified complete or substantially complete.
4. Move report to `docs/archive/tasks/canceled/` if superseded, invalidated, or no longer applicable.

### Required Update Rules
On every audit create/move/close action, update all of the following in the same change:
1. Source stage `ROUTER.md` (if moving from a stage)
2. Destination stage `ROUTER.md`
3. `docs/archive/tasks/ROUTER.md`
4. `docs/archive/tasks/README.md` (index and status)
5. `docs/archive/ROUTER.md` when structure or purpose changes

### Naming Rules
- Use cycle-prefixed audit filenames:
  - `C{NN}-{TYPE}-{ID}.md`
- Example patterns:
  - `C01-QA-AUDIT-001.md`
  - `C01-SEC-AUDIT-001.md`
  - `C02-PERF-AUDIT-001.md`

### Scope and Content Rules
- Each audit must capture: scope, findings, severity, remediation plan, verification result, and final status.
- Keep artifacts focused; split overly large reports into logically grouped files.
- Do not store transient planning chatter inside archive reports.

### Environment Baseline Rules (for audit context)
- Service Manager API endpoint: `http://192.168.23.83:3500`
- Shopify dev service endpoint: `http://192.168.23.83:9292`
- Preserve these endpoints in audit evidence unless the environment is intentionally changed.

## Common Pitfalls
1. **LAN HTTP** — The service manager runs plain HTTP on LAN. Use `network_security_config.xml` with a `<domain>` exception for the LAN host rather than enabling cleartext globally.
2. **ViewModel scope** — Don't launch coroutines from Activity; always use `viewModelScope`.
3. **Base URL** — The server IP is user-configurable; never hardcode `192.168.x.x`. Read from `EncryptedSharedPreferences`.
4. **Rotation** — Use `StateFlow`/`LiveData` so UI survives config changes without redundant API calls.
5. **Release signing** — `keystore.properties` is gitignored. CI must inject it via secrets.
6. **Hilt missing `@AndroidEntryPoint`** — Every Fragment/Activity using Hilt injection must be annotated; forgetting causes runtime crashes.
7. **Missing `repeatOnLifecycle`** — Collecting StateFlow without `repeatOnLifecycle(STARTED)` keeps collecting in background, draining battery.
8. **Navigation with `startActivity()`** — Use `findNavController().navigate(R.id.action_*)` instead.
9. **Missing ProGuard rules** — Retrofit/Gson model classes get stripped without explicit keep rules in `proguard-rules.pro`.
10. **Flow test without Turbine** — Never `collect` in a launch in tests; use `turbine` or `toList()` with `take()`.

## Checklist Before Exit
- `Prompt.md` updated with accurate summary and NEXT pointer status.
- `ops/NEXT.yaml` task queue reflects current work state.
- `Tasklist.md` updated with task status changes.
- Routers synchronized if structural changes made.
- Commit message references task ID and change summary.
- All new ViewModels/Repositories have unit tests.
- `./gradlew lint ktlintCheck` passes with no errors.

## Done Criteria (Per Task)
- Changes implemented and verified.
- Knowledge updated (`docs/` + this guide if behavior changed).
- Routers updated if structural changes.
- Prompt.md PATCHSET echo current.

---
**Document Version:** 1.2 (2026-04-23) — Added explicit audit system rules and lifecycle governance
**Last Updated:** 2026-04-23T00:00:00Z
