# Architecture Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [System Overview](01-system-overview.md) | Pending |
| 02 | [Android Architecture](02-android-architecture.md) | Pending |
| 03 | [API Integration](03-api-integration.md) | Pending |
| 04 | [Security Architecture](04-security-architecture.md) | Pending |

## Overview

The Service Manager Android App is a native Android client built with Kotlin and MVVM architecture. It communicates exclusively with the Service Manager REST API (`http://<host>:3500/api/`) over the local area network.

### Architecture Layers
- **UI Layer:** Activities, Fragments, ViewBinding — no business logic
- **ViewModel Layer:** Exposes `StateFlow<UiState>`, lives in `viewModelScope`
- **Repository Layer:** `ServiceRepository` — single source of truth for all API data
- **Network Layer:** Retrofit + OkHttp, base URL from `EncryptedSharedPreferences`

### Key Design Decisions
- **Configurable server URL** — The app never hardcodes `192.168.x.x`. Server address is entered by the user on first run and stored securely.
- **LAN HTTP** — The server runs plain HTTP. A `network_security_config.xml` domain exception permits cleartext for the user-configured host only. `usesCleartextTraffic` is not enabled globally.
- **No server-side components** — This repo is Android-only. The Node.js server lives at [Sherbivs/service-manager](https://github.com/Sherbivs/service-manager).
