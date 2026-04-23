# Architecture Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [System Overview](01-system-overview.md) | Pending |
| 02 | [Android Architecture (MVVM + UDF)](02-android-architecture.md) | Pending |
| 03 | [API Integration](03-api-integration.md) | Pending |
| 04 | [Security Architecture](04-security-architecture.md) | Pending |

---

## Layered Architecture

The app follows the official Android layered architecture. Each layer has a single responsibility and communicates only with the layer immediately below it.

```
UI Layer
  ├── MainActivity (NavHostFragment)
  ├── ServicesFragment  ← observes StateFlow, dispatches events
  ├── SystemFragment
  ├── LogsFragment
  └── SettingsFragment

ViewModel Layer (survives rotation)
  ├── ServicesViewModel  ← holds StateFlow<ServicesUiState>
  ├── SystemViewModel
  └── LogsViewModel

[Domain Layer] (optional; add only for complex reusable logic)
  └── GetServicesUseCase, etc.

Data Layer
  ├── ServiceRepository  ← single source of truth
  ├── ApiService (Retrofit interface)
  └── EncryptedPrefsHelper
```

**Layer rules (strictly enforced):**
- UI layer: ViewBinding setup, click listeners, StateFlow collection — NOTHING ELSE
- ViewModel layer: `viewModelScope` coroutines, state transformation, business rules — no `Context`
- Repository layer: API calls, result mapping, error handling — no Android UI classes
- DTOs vs Models: API returns `ServiceDto`; ViewModel receives `ServiceModel` (mapped in Repository)

---

## Unidirectional Data Flow (UDF)

All state changes follow a single direction: **User Event → ViewModel → StateFlow → UI**.

```
User taps "Start"  →  Fragment calls viewModel.startService(id)
                   →  ViewModel sets state to Loading, calls repo.startService(id)
                   →  Repo POST /api/services/:id/start → returns Result<Unit>
                   →  ViewModel sets state to Success or Error
                   →  Fragment's collect{} re-renders UI from new state
```

**Required sealed class pattern per screen:**
```kotlin
sealed class ServicesUiState {
    object Loading : ServicesUiState()
    data class Success(val services: List<ServiceModel>) : ServicesUiState()
    data class Error(val message: String) : ServicesUiState()
}
```

**Never** expose `MutableStateFlow` from a ViewModel. Always use `.asStateFlow()`.

---

## Dependency Injection (Hilt)

Hilt is the official Android DI library (built on Dagger 2). It provides:
- Compile-time DI graph validation
- Automatic lifecycle scoping (`SingletonComponent`, `ViewModelComponent`, `ActivityComponent`)
- `@HiltViewModel` for ViewModel injection (no `ViewModelProvider.Factory` boilerplate)

**Module structure:**
```
di/
  NetworkModule.kt    — Retrofit, OkHttpClient, ApiService (@Singleton)
  AppModule.kt        — EncryptedPrefsHelper, any other app-wide deps
```

**Scoping:**
- `@Singleton` — Retrofit, OkHttpClient, ApiService, ServiceRepository, EncryptedPrefsHelper
- `@HiltViewModel` + `@Inject` — All ViewModels
- `@ActivityScoped` — Only if a dependency must be tied to a single Activity

---

## Navigation (Jetpack Navigation Component)

Single-activity architecture. `MainActivity` hosts one `NavHostFragment`. All screens are Fragments.

```
MainActivity
  └── NavHostFragment (nav_graph.xml)
        ├── ServicesFragment   (startDestination)
        ├── SystemFragment
        ├── LogsFragment
        └── SettingsFragment
```

**Nav graph actions (example):**
```xml
<action
    android:id="@+id/action_services_to_logs"
    app:destination="@id/logsFragment" />
```

**BottomNavigationView** wired to `NavController` via `NavigationUI.setupWithNavController()`.

---

## Optional Domain Layer (Use Cases)

Use Cases belong in `domain/` and are only added when business logic is:
- **Shared** between multiple ViewModels, OR
- **Complex enough** that the ViewModel exceeds ~80 lines of business logic

For this app, Use Cases are NOT required in initial implementation.

---

## Key Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| State exposure | `StateFlow<UiState>` | Kotlin-native, lifecycle-aware, testable with Turbine |
| DI framework | Hilt | Official Android recommendation, compile-time safe |
| Navigation | Navigation Component | Single back stack, Safe Args, deep link ready |
| Network | Retrofit + OkHttp | Industry standard, Hilt-injectable, MockWebServer testable |
| Secrets | EncryptedSharedPreferences | MASVS M2 compliant, hardware-backed on API 23+ |
| Cleartext | domain exception only | MASVS M3; `usesCleartextTraffic` never global |
| View system | ViewBinding | Null-safe, no reflection, simpler than DataBinding |
| Background | foreground polling only | WorkManager added only if background sync is needed |
| UI tests | FragmentScenario + Espresso | Official; no Robolectric dependency |
