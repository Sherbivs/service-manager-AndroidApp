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
    MainActivity.kt       — Single activity; hosts NavHostFragment
    di/                   — Hilt modules (NetworkModule, AppModule)
    ui/
      services/           — ServicesFragment, ServicesViewModel
      system/             — SystemFragment, SystemViewModel
      logs/               — LogsFragment, LogsViewModel
      settings/           — SettingsFragment, SettingsViewModel
    data/
      api/                — ApiService.kt (Retrofit interface)
      model/              — DTOs (ServiceDto, SystemInfoDto, etc.)
      repository/         — ServiceRepository.kt
    domain/               — Use Cases (optional; only for complex shared logic)
    util/                 — EncryptedPrefsHelper, Extensions
  res/
    layout/               — XML layouts
    navigation/           — nav_graph.xml (Navigation Component)
    values/               — strings, colors, themes
    xml/                  — network_security_config.xml
  AndroidManifest.xml
ops/             — Governance (NEXT.yaml, ROUTER.yaml, TOUCHMAP.yaml)
docs/            — Bible documentation
```

### Key Design Principles
- **MVVM + UDF** — State flows *down* (ViewModel → UI); events flow *up* (UI → ViewModel).
- **Single activity** — `MainActivity` hosts a `NavHostFragment`. All screens are Fragments.
- **Hilt DI** — All dependencies injected via Hilt. No manual `new Retrofit(...)` in ViewModels.
- **Configurable base URL** — Never hardcode server IP. Read from `EncryptedSharedPreferences`.
- **No coroutines in Activities** — Always use `viewModelScope` for coroutine launches.
- **ViewBinding only** — No `findViewById`, no data binding.
- **One `StateFlow<UiState>` per screen** — ViewModel exposes a single sealed-class UiState.

### Unidirectional Data Flow (UDF) — Required Pattern

```kotlin
// Sealed UiState — one per screen
sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<ServiceModel>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

// ViewModel — private mutable, public immutable StateFlow
@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repo: ServiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    fun load() = viewModelScope.launch {
        _uiState.value = ServicesUiState.Loading
        repo.getServices()
            .onSuccess { _uiState.value = ServicesUiState.Success(it) }
            .onFailure { _uiState.value = ServicesUiState.Error(it.message ?: "Unknown error") }
    }
}

// Fragment — collect with lifecycle awareness (REQUIRED — prevents background collection)
viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { render(it) }
    }
}
```

### Dependency Injection — Hilt (Required)

```kotlin
@HiltAndroidApp class ServiceManagerApp : Application()

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
  fun provideRetrofit(prefs: EncryptedPrefsHelper): Retrofit =
    Retrofit.Builder().build()
    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}

// Every Fragment/Activity using Hilt MUST be annotated
@AndroidEntryPoint class ServicesFragment : Fragment()

// Every ViewModel MUST use @HiltViewModel + @Inject
@HiltViewModel
class ServicesViewModel @Inject constructor(private val repo: ServiceRepository) : ViewModel()
```

### Navigation — Jetpack Navigation Component (Required)
- Single `NavHostFragment` in `MainActivity`
- All screens are Fragments; navigation declared in `res/navigation/nav_graph.xml`
- Use `findNavController().navigate(R.id.action_*)` — never `startActivity()` for in-app navigation
- Use Safe Args plugin for type-safe argument passing between destinations

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
- **Logging:** Strip sensitive data from Logcat in release builds (use `BuildConfig.DEBUG` guards).

### OWASP MASVS Alignment
- M1 (Improper Platform Usage): Use runtime permissions for dangerous perms; declare minimum perms.
- M2 (Insecure Data Storage): EncryptedSharedPreferences for all sensitive values.
- M3 (Insecure Communication): TLS 1.2+; network_security_config blocks cleartext globally.
- M9 (Reverse Engineering): R8 obfuscation on release; no sensitive strings in code.

## Testing Requirements (Every Feature)

Every feature is **NOT DONE** until it has tests. Required per feature:

### Unit Tests (`app/src/test/`) — JUnit 4, MockK, Turbine, kotlinx-coroutines-test
```kotlin
@Test fun `load services emits Success`() = runTest {
    val repo = mockk<ServiceRepository> {
        coEvery { getServices() } returns Result.success(fakeServices)
    }
    val vm = ServicesViewModel(repo)
    vm.uiState.test {
        assertEquals(ServicesUiState.Loading, awaitItem())
        vm.load()
        assertEquals(ServicesUiState.Success(fakeServices), awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Integration Tests (`app/src/test/` + MockWebServer)
- Use `okhttp3.mockwebserver.MockWebServer` to simulate the Service Manager API
- Test `ServiceRepository` with real Retrofit + fake server
- Assert JSON parsing, HTTP error handling (4xx, 5xx, timeouts)

### UI / Instrumented Tests (`app/src/androidTest/`) — Espresso + FragmentScenario
- `FragmentScenario.launchInContainer<ServicesFragment>()` — test in isolation
- Only assert user-visible behaviour (text, button states)

### Coverage Targets
- ViewModel classes: ≥80% line coverage
- Repository classes: ≥80% line coverage

## Code Quality Gates (Required Before Any Merge)

```bash
./gradlew lint          # Zero errors (abortOnError true)
./gradlew ktlintCheck   # Zero style violations
./gradlew detekt        # Zero ERRORs
./gradlew test          # All unit tests pass
```

Auto-fix before committing:
```bash
./gradlew ktlintFormat
```

## Development Workflows

### Common Commands
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (requires keystore)
./gradlew installDebug           # Install on connected device
./gradlew lint                   # Android Lint
./gradlew ktlintCheck            # Kotlin style check
./gradlew ktlintFormat           # Auto-fix style
./gradlew detekt                 # Static analysis
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (device/emulator)
```

### Adding a New Feature (Checklist)
1. Create `*UiState` sealed class in `ui/<feature>/`
2. Create `*ViewModel` (`@HiltViewModel`) in `ui/<feature>/`
3. Create `*Fragment` (`@AndroidEntryPoint`) in `ui/<feature>/`
4. Add navigation destination to `res/navigation/nav_graph.xml`
5. Add API endpoint to `data/api/ApiService.kt` if needed
6. Add repository method to `data/repository/ServiceRepository.kt` if needed
7. Write `*ViewModelTest` + `*RepositoryIntegrationTest` in `src/test/`
8. Write `*FragmentTest` in `src/androidTest/`
9. Update `ROUTER.md` for any new directories
10. Run: `./gradlew lint ktlintCheck detekt test`

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
1. **LAN HTTP** — Use a domain exception in `network_security_config.xml`, not global cleartext.
2. **ViewModel scope** — `viewModelScope.launch {}` only. Never `GlobalScope` or `lifecycleScope` from Activity.
3. **Base URL trailing slash** — Retrofit requires base URL to end with `/`.
4. **Missing `repeatOnLifecycle`** — Without it, StateFlow collection continues in background, draining battery.
5. **Hilt missing `@AndroidEntryPoint`** — Forgetting this annotation causes runtime crashes.
6. **Navigation with `startActivity()`** — Use `findNavController().navigate(R.id.action_*)` instead.
7. **Missing ProGuard rules** — Retrofit/Gson model classes get stripped without explicit keep rules.
8. **Flow test without Turbine** — Never `collect` in a `launch` in tests; use Turbine.
9. **Rotation** — ViewModel survives rotation; don't re-fetch if data is fresh.
10. **Keystore** — `keystore.properties` must never be committed. Document format in `docs/operations-bible/`.

