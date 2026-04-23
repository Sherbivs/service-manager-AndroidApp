# Development Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [Local Setup](01-local-setup.md) | Pending |
| 02 | [Coding Conventions](02-coding-conventions.md) | Pending |
| 03 | [Testing Guide](03-testing.md) | Pending |
| 04 | [Contributing Guide](04-contributing.md) | Pending |

---

## Quick Setup

### Prerequisites
- Android Studio Hedgehog 2023.1.1+ (or later)
- JDK 17+ (bundled with Android Studio)
- Android SDK (API 24–34) — installed via SDK Manager
- Physical device or emulator (API 24+)
- Service Manager server running on LAN ([Sherbivs/service-manager](https://github.com/Sherbivs/service-manager))

### Clone & Build
```bash
git clone https://github.com/Sherbivs/service-manager-AndroidApp.git
cd service-manager-AndroidApp
./gradlew assembleDebug
./gradlew installDebug
```

---

## Coding Conventions

### Kotlin Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Style enforced by **ktlint** — run `./gradlew ktlintFormat` before committing
- Static analysis by **detekt** — zero ERRORs policy, review WARNINGs

### Architecture Rules (hard constraints)
| Location | Allowed | Forbidden |
|----------|---------|----------|
| Fragment/Activity | ViewBinding, click listeners, StateFlow collection | Business logic, coroutines, API calls |
| ViewModel | `viewModelScope`, state, business rules | `Context`, Android UI classes |
| Repository | API calls, result mapping, error handling | ViewModel references, UI classes |
| DTOs (`model/`) | `data class` JSON shapes | Domain logic |

### File Naming
```
ServicesViewModel.kt          # ViewModel (+ UiState in same file)
ServicesFragment.kt           # Fragment
ServiceDto.kt                 # Network DTO (data/model/)
ServiceModel.kt               # Domain model (mapped from DTO in Repository)
ServicesViewModelTest.kt      # ViewModel unit test (src/test/)
ServiceRepositoryTest.kt      # Repository integration test (src/test/, MockWebServer)
ServicesFragmentTest.kt       # Fragment UI test (src/androidTest/, Espresso)
```

### Sealed UiState Pattern (required on every screen)
```kotlin
sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<ServiceModel>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}

// ViewModel — private mutable, public immutable
@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repo: ServiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()
}

// Fragment — ALWAYS use repeatOnLifecycle
viewLifecycleOwner.lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { render(it) }
    }
}
```

---

## Testing Guide

### Testing Pyramid
```
          [UI Tests]        — Espresso / FragmentScenario  (fewest, device)
       [Integration Tests]  — MockWebServer + Repository   (medium, JVM)
    [Unit Tests]            — JUnit + MockK + Turbine       (most, JVM)
```

### Required test files per feature
| Feature | Unit test | Integration test | UI test |
|---------|-----------|-----------------|--------|
| ServicesScreen | `ServicesViewModelTest` | `ServiceRepositoryTest` | `ServicesFragmentTest` |
| SystemScreen | `SystemViewModelTest` | `ServiceRepositoryTest` | `SystemFragmentTest` |
| LogsScreen | `LogsViewModelTest` | (covered by Repository test) | `LogsFragmentTest` |

### Coverage Targets
| Class | Target |
|-------|--------|
| ViewModel classes | ≥ 80% line coverage |
| Repository classes | ≥ 80% line coverage |

### Unit Test Dependencies
```groovy
testImplementation 'junit:junit:4.13.2'
testImplementation 'io.mockk:mockk:1.13.9'
testImplementation 'app.cash.turbine:turbine:1.0.0'
testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3'
testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'
```

### `MainDispatcherRule` (required in every ViewModel test)
```kotlin
// app/src/test/java/com/servicemanager/app/util/MainDispatcherRule.kt
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(d: Description?) = Dispatchers.setMain(dispatcher)
    override fun finished(d: Description?) = Dispatchers.resetMain()
}
```

---

## Code Quality Gates

```bash
./gradlew lint          # Zero errors (abortOnError true)
./gradlew ktlintCheck   # Zero style violations
./gradlew ktlintFormat  # Auto-fix (run before commit)
./gradlew detekt        # Zero ERRORs
./gradlew test          # All unit tests pass
```

### detekt.yml key thresholds
- `complexity.CyclomaticComplexity.threshold: 15`
- `complexity.LongMethod.threshold: 40`
- `complexity.TooManyFunctions.thresholdInClasses: 11`
- `style.MagicNumber.ignoreNumbers: [-1, 0, 1, 2, 100]`

---

## CI/CD (GitHub Actions, SMA.013)

**PR trigger:** `lint → ktlintCheck → detekt → test`  
**Push to `main`:** `assembleDebug → upload APK artifact (7-day retention)`  
**Tag trigger:** `assembleRelease (keystore from GitHub Secrets) → upload signed APK`

---

## Commit Messages
```
[Task SMA.XXX] Brief one-line summary

Why: Reason for the change
Changes: Files modified and what changed
Testing: How it was verified
```
