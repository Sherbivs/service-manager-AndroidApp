# Development Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [Local Setup](01-local-setup.md) | Pending |
| 02 | [Coding Conventions](02-coding-conventions.md) | Pending |
| 03 | [Contributing Guide](03-contributing.md) | Pending |
| 04 | [Testing Guide](04-testing-guide.md) | Pending |
| 05 | [Code Quality & CI](05-code-quality-ci.md) | Pending |

## Quick Setup

### Prerequisites
- Android Studio Hedgehog 2023.1.1+ (or later)
- JDK 17+ (bundled with Android Studio)
- Android SDK (API 24–34) — installed via SDK Manager
- Physical device or emulator (API 24+)
- Service Manager server running on LAN ([Sherbivs/service-manager](https://github.com/Sherbivs/service-manager))

### First Run
```bash
git clone https://github.com/Sherbivs/service-manager-AndroidApp.git
cd service-manager-AndroidApp
# Open in Android Studio OR:
./gradlew assembleDebug
./gradlew installDebug
```

## Coding Conventions

### Kotlin
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `data class` for API response DTOs
- Prefer `sealed class` for UI state: `Loading`, `Success(data)`, `Error(message)`
- Extension functions go in `util/Extensions.kt`
- `object` for singletons; `companion object` only for factory methods or constants

### Architecture Rules
- Activities and Fragments contain ONLY: ViewBinding setup, ViewModel observation, click listeners
- ViewModels contain ONLY: business logic, coroutine launches, state management
- Repository contains ONLY: API calls, data transformation, caching
- Never access `Context` from a ViewModel — use `AndroidViewModel` only if absolutely necessary
- All ViewModel dependencies injected via Hilt `@Inject constructor` — never `ViewModelFactory` manually
- All screen transitions via `NavController.navigate()` with Safe Args — never `startActivity()`

### File Naming
- ViewModels: `ServicesViewModel.kt`, `SystemViewModel.kt`
- Fragments: `ServicesFragment.kt`, `SystemFragment.kt`
- Activities: `MainActivity.kt`
- API DTOs: `ServiceDto.kt`, `SystemInfoDto.kt`
- Domain models: `ServiceModel.kt`, `SystemInfoModel.kt`
- Hilt modules: `NetworkModule.kt`, `DataModule.kt`

## Testing Standards

### Test Layers

| Layer | Location | Frameworks | Minimum Coverage |
|-------|----------|------------|-----------------|
| Unit | `app/src/test/` | JUnit 5, MockK, Turbine, kotlinx-coroutines-test | ≥80% ViewModel + Repository |
| Integration | `app/src/test/` | OkHttp MockWebServer | All API endpoints |
| UI / Smoke | `app/src/androidTest/` | Espresso, TestNavHostController | 1 smoke test per screen |

### Unit Test Template (ViewModel)
```kotlin
@ExtendWith(InstantTaskExecutorExtension::class)
class ServicesViewModelTest {
    private val repository: ServiceRepository = mockk()
    private lateinit var viewModel: ServicesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ServicesViewModel(repository)
    }

    @Test
    fun `loadServices emits Success when repository returns data`() = runTest {
        val services = listOf(ServiceModel("id", "name", "running"))
        coEvery { repository.getServices() } returns Result.success(services)

        viewModel.uiState.test {
            viewModel.loadServices()
            assertEquals(ServicesUiState.Loading, awaitItem())
            assertEquals(ServicesUiState.Success(services), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Key Testing Dependencies
```kotlin
// Unit testing
testImplementation("junit:junit:4.13.2")
testImplementation("io.mockk:mockk:1.13.x")
testImplementation("app.cash.turbine:turbine:1.x.x")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.x.x")
// Integration (MockWebServer)
testImplementation("com.squareup.okhttp3:mockwebserver:4.x.x")
// UI tests
androidTestImplementation("androidx.test.espresso:espresso-core:3.x.x")
androidTestImplementation("androidx.navigation:navigation-testing:2.x.x")
```

## Code Quality Tools

All four must pass before any commit. CI blocks PRs if they fail.

| Tool | Command | Config File | Purpose |
|------|---------|-------------|---------|
| Android Lint | `./gradlew lint` | `lint.xml` | Android anti-patterns, resource issues |
| ktlint | `./gradlew ktlintCheck` | `.editorconfig` | Kotlin formatting |
| Detekt | `./gradlew detekt` | `detekt.yml` | Complexity, code smells, SOLID violations |
| Unit Tests | `./gradlew test` | — | Correctness |

### Run All Locally
```bash
./gradlew lint ktlintCheck detekt test
```

### CI/CD (GitHub Actions)
Workflow at `.github/workflows/ci.yml` triggers on push + PR to `main`:
1. Checkout code
2. Setup JDK 17
3. Cache Gradle dependencies
4. `./gradlew lint`
5. `./gradlew ktlintCheck`
6. `./gradlew detekt`
7. `./gradlew test`

PRs cannot be merged if any step fails.

## Commit Messages
```
[Task SMA.XXX] Brief one-line summary

Why: Reason for the change
Changes: Files modified and what changed
Testing: How it was verified
```
