# Android Architecture (MVVM + UDF)
**Project:** Service Manager Android App
**Status:** Active
**Last Updated:** 2026-04-28

## Pattern Contract
The app uses MVVM with Unidirectional Data Flow.

Rules:
- UI emits events upward.
- ViewModel performs business actions and emits state downward.
- Repository is the only data access boundary.
- Mutable state is never exposed publicly.

## Layer Responsibilities
1. UI Layer (Activity/Fragment)
- Render UiState.
- Wire click handlers and user events.
- Collect flows with lifecycle awareness.

2. ViewModel Layer
- Hold one StateFlow<UiState> per screen.
- Launch work in viewModelScope.
- Map repository Result to Loading/Success/Error states.

3. Data Layer
- Execute network requests.
- Normalize errors and return Result<T>.
- Provide DTO models for API contracts.

## Screen State Model
Each screen follows a sealed UiState pattern:
```kotlin
sealed class LogsUiState {
	object Loading : LogsUiState()
	data class Success(val lines: List<String>) : LogsUiState()
	data class Error(val message: String) : LogsUiState()
}
```

## Lifecycle-Safe Collection
Fragments collect flows using repeatOnLifecycle(STARTED) to avoid background collection when screens are not visible.

Expected pattern:
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
	repeatOnLifecycle(Lifecycle.State.STARTED) {
		viewModel.uiState.collect { render(it) }
	}
}
```

## Scalability Behaviors in Architecture
Recent hardening integrated into the architecture:
- Archive search request cancellation before starting a new query.
- Dialog-bound archive collectors canceled on dismiss.
- Archive pagination state tracked by ViewModel (limit/offset/total).
- Local log filtering debounced and moved off the main thread.

These behaviors prevent redundant work and reduce UI jank under larger data volumes.

## DI and Composition
- Hilt composes network, repository, and utility singletons.
- ViewModels are constructor-injected.
- Fragment/Activity classes requiring DI use AndroidEntryPoint.

## Testing Alignment
Architecture is designed for testability:
- ViewModel behavior tested via coroutine test dispatcher + Turbine.
- Repository behavior tested with MockWebServer.
- UI behavior validated with FragmentScenario and Espresso.
