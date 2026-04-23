# Architecture Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [System Overview](01-system-overview.md) | Pending |
| 02 | [Android Architecture & UDF](02-android-architecture.md) | Pending |
| 03 | [API Integration](03-api-integration.md) | Pending |
| 04 | [Security Architecture](04-security-architecture.md) | Pending |
| 05 | [Dependency Injection (Hilt)](05-dependency-injection.md) | Pending |
| 06 | [Navigation Component](06-navigation.md) | Pending |
| 07 | [Testing Strategy](07-testing-strategy.md) | Pending |

## Architecture Overview

The Service Manager Android App is a native Android client built with Kotlin using **MVVM** architecture and **Unidirectional Data Flow (UDF)**. It communicates with the Service Manager REST API (`http://<host>:3500/api/`) over LAN.

### Layer Responsibilities

| Layer | Components | Rules |
|-------|-----------|-------|
| **UI** | Activities, Fragments, ViewBinding | No business logic. Only: observe `StateFlow`, call ViewModel methods, setup ViewBinding. |
| **ViewModel** | `@HiltViewModel` classes | Exposes `val uiState: StateFlow<UiState>`. Launches coroutines in `viewModelScope`. No `Context` access. |
| **Repository** | `ServiceRepository` | Single source of truth. Wraps Retrofit, maps DTOs to domain models, returns `Result<T>`. |
| **Network** | `ApiService`, `RetrofitClient` | Retrofit interface + OkHttp client. Base URL from `EncryptedSharedPreferences`. |
| **Util** | `EncryptedPrefsHelper` | Wraps `EncryptedSharedPreferences`; no raw `SharedPreferences` for sensitive values. |

### Unidirectional Data Flow (UDF)

All screens follow this mandatory pattern:
```
User Event → Fragment/Activity calls ViewModel.onEvent()
           → ViewModel updates MutableStateFlow
           → Fragment collects StateFlow
           → UI renders new state
```

Sealed state class template:
```kotlin
sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<ServiceModel>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}
```

ViewModel template:
```kotlin
@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val repository: ServiceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    fun loadServices() {
        viewModelScope.launch {
            _uiState.value = ServicesUiState.Loading
            repository.getServices()
                .onSuccess { _uiState.value = ServicesUiState.Success(it) }
                .onFailure { _uiState.value = ServicesUiState.Error(it.message ?: "Unknown error") }
        }
    }
}
```

### Architecture Patterns: MVVM vs MVI vs Clean

| Pattern | Description | Pros | Cons | When to Use |
|---------|-------------|------|------|-------------|
| **MVVM + UDF** | ViewModel holds state as StateFlow; UI observes. Current approach. | Jetpack-native, easy to test, survives rotation. | Risk of bloated VM if not careful. | All screens in this app. |
| **MVI** | Single immutable state per screen; Intents reduce to new state (Redux-like). | Very predictable; great for complex screens. | More boilerplate; steeper learning curve. | Consider if a screen has >5 distinct event types. |
| **Clean (Domain layer)** | Adds Use Case classes between ViewModel and Repository. | Reusable business logic; very testable. | More files/interfaces. | Add when a ViewModel has >3 concerns (see SMA.105). |

### Dependency Injection (Hilt)

All dependencies wired via Hilt:
- `@HiltAndroidApp` on `Application` class
- `@AndroidEntryPoint` on every `Activity` and `Fragment`
- `@HiltViewModel` + `@Inject constructor` on every ViewModel
- `NetworkModule` provides `OkHttpClient`, `Retrofit`, `ApiService`
- `DataModule` provides `ServiceRepository`, `EncryptedPrefsHelper`

**Never** manually instantiate `ServiceRepository` or `RetrofitClient`.

### Navigation

Single-activity architecture via Jetpack Navigation Component:
- `MainActivity` hosts one `NavHostFragment`
- `res/navigation/nav_graph.xml` defines all destinations
- Safe Args generates typed direction classes
- All transitions via `NavController.navigate()` — no `startActivity()` for in-app nav
- Back stack managed by NavController

### Key Design Decisions
- **Configurable server URL** — Never hardcoded. User enters on first run; stored in `EncryptedSharedPreferences`.
- **LAN HTTP** — Server uses plain HTTP. `network_security_config.xml` allows cleartext for the configured host only. `usesCleartextTraffic` is not enabled globally.
- **No server-side components** — Android-only. Node.js server at [Sherbivs/service-manager](https://github.com/Sherbivs/service-manager).
- **Future: Domain layer** — Extract Use Case classes if ViewModel complexity grows (see SMA.105 in Tasklist).
- **Future: Modularization** — `:core`, `:data`, `:features:services` modules if build times or team size grow (see SMA.106).
