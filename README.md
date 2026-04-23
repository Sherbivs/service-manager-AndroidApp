# Service Manager — Android App

Native Android companion app for the [Service Manager](https://github.com/Sherbivs/service-manager) dashboard. Monitor and control your local dev services from any Android device on your LAN.

## Features

- **Live service status** — see running/stopped state, health, PID, and uptime
- **One-tap control** — Start, Stop, Restart any managed service
- **System overview** — hostname, IP, memory, Node version, uptime
- **Log viewer** — tail live logs and search the archive
- **Configurable server URL** — point to any Service Manager instance on your LAN
- **Secure storage** — server URL and credentials stored in EncryptedSharedPreferences
- **Material 3 design** — dark-theme ready, responsive layout

## Requirements

- Android 7.0+ (API 24)
- Service Manager server running at `http://192.168.23.83:3500` ([Sherbivs/service-manager](https://github.com/Sherbivs/service-manager))
- Shopify dev service (TCB Party Rental) running at `http://192.168.23.83:9292`
- Android Studio Hedgehog 2023.1.1+ (for development)

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug
```

Or open in Android Studio and press **Run**.

## Configuration

On first launch, enter your Service Manager server URL (use `http://192.168.23.83:3500` for this environment). This is stored securely via `EncryptedSharedPreferences`.

## Architecture

```
Android App (Kotlin, MVVM)
  ↕ Retrofit / OkHttp
Service Manager API  (http://192.168.23.83:3500)
  ↕
Managed Service Processes
```

## Environment Baseline

- Service Manager API: `http://192.168.23.83:3500`
- Shopify Dev Service (TCB Party Rental): `http://192.168.23.83:9292`
- Service health checks are evaluated server-side and may use loopback targets (for example `http://127.0.0.1:9292`).

**Stack:** Kotlin, MVVM, Retrofit, OkHttp, ViewBinding, Material 3, `androidx.security.crypto`

## Project Structure

```
app/src/main/
  java/com/servicemanager/app/
    ui/          — Activities, Fragments, ViewModels
    data/        — API client, models, repository
    util/        — Shared helpers
  res/           — Layouts, drawables, strings, themes
  AndroidManifest.xml
```

## Security

- All secrets stored in Android Keystore / EncryptedSharedPreferences
- Network security configured via `network_security_config.xml`
- Release builds use R8 minification + resource shrinking
- `android:debuggable="false"` enforced in release
- Keystore files and `keystore.properties` are gitignored

## Companion Repository

The Node.js service manager server lives at **[Sherbivs/service-manager](https://github.com/Sherbivs/service-manager)**.
This app is the Android client only — it has no server-side components.

## License

MIT

## License

MIT
