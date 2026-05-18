# Service Manager — Android App

Native Android companion app for the [Service Manager](https://github.com/Sherbivs/service-manager) dashboard. Monitor and control your local dev services from any Android device on your LAN.

## Features

- **Live service status** — see running/stopped state, health, PID, and uptime
- **One-tap control** — Start, Stop, Restart any managed service
- **System overview** — hostname, IP, memory, Node version, uptime
- **Log viewer** — tail live logs and search the archive
- **Configurable network settings** — set scheme/host/port and request timeouts from Settings
- **Secure storage** — network settings and credentials stored in Secure DataStore + Tink
- **Material 3 design** — dark-theme ready, responsive layout

## Requirements

- Android 7.0+ (API 24)
- Build target SDK: 37
- Service Manager server running at `http://sensaimanager.drip:3500` ([Sherbivs/service-manager](https://github.com/Sherbivs/service-manager))
- Shopify dev services routed at `http://tcb.drip` and `http://blt.drip`
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

On first launch, enter your Service Manager endpoint. In Settings, you can permanently adjust scheme, host, port, and connect/read timeout values. Baseline endpoint for this environment is `http://sensaimanager.drip:3500`. These settings are stored securely via Jetpack DataStore + Tink.

## Architecture

```
Android App (Kotlin, MVVM)
  ↕ Retrofit / OkHttp
Service Manager API  (http://sensaimanager.drip:3500)
  ↕
Managed Service Processes
```

## Environment Baseline

- Service Manager API: `http://sensaimanager.drip:3500` (LAN IP `192.168.23.106`)
- Shopify Dev Services: `http://tcb.drip` and `http://blt.drip`
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

- All secrets stored in Android Keystore / Secure DataStore + Tink
- Network security configured via `network_security_config.xml`
- Release builds use R8 minification + resource shrinking
- `android:debuggable="false"` enforced in release
- Keystore files and `keystore.properties` are gitignored

## Companion Repository

The Node.js service manager server lives at **[Sherbivs/service-manager](https://github.com/Sherbivs/service-manager)**.
This app is the Android client only — it has no server-side components.

## License

MIT
