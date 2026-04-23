# Development Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [Local Setup](01-local-setup.md) | Pending |
| 02 | [Coding Conventions](02-coding-conventions.md) | Pending |
| 03 | [Contributing Guide](03-contributing.md) | Pending |

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
- Use `data class` for API response models
- Prefer `sealed class` for UI state: `Loading`, `Success(data)`, `Error(message)`
- Extension functions go in `util/Extensions.kt`

### Architecture Rules
- Activities and Fragments contain ONLY: ViewBinding setup, ViewModel observation, click listeners
- ViewModels contain ONLY: business logic, coroutine launches, state management
- Repository contains ONLY: API calls, data transformation, caching
- Never access `Context` from a ViewModel — use `AndroidViewModel` only if absolutely necessary

### File Naming
- ViewModels: `ServicesViewModel.kt`, `SystemViewModel.kt`
- Fragments: `ServicesFragment.kt`, `SystemFragment.kt`
- Activities: `MainActivity.kt`, `SettingsActivity.kt`
- API models: `ServiceDto.kt`, `SystemInfoDto.kt`

### Commit Messages
```
[Task SMA.XXX] Brief one-line summary

Why: Reason for the change
Changes: Files modified and what changed
Testing: How it was verified
```
