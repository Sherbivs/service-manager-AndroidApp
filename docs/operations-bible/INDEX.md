# Operations Bible — Index
**Project:** Service Manager Android App

## Documents

| # | Title | Status |
|---|-------|--------|
| 01 | [Build & Install](01-build-install.md) | Pending |
| 02 | [Release Signing](02-release-signing.md) | Pending |
| 03 | [Play Store Deployment](03-play-store.md) | Pending |
| 04 | [Troubleshooting](04-troubleshooting.md) | Pending |

## Quick Reference

### Build Commands
```bash
./gradlew assembleDebug          # Debug APK → app/build/outputs/apk/debug/
./gradlew assembleRelease        # Release APK (requires keystore)
./gradlew installDebug           # Install debug on connected device
./gradlew lint                   # Run Android Lint
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests (device/emulator required)
```

### keystore.properties Format
```properties
storeFile=../my-release.keystore
storePassword=<store_password>
keyAlias=<key_alias>
keyPassword=<key_password>
```
**This file is gitignored. Never commit it.**

### Connecting to Service Manager
1. Ensure the [Service Manager server](https://github.com/Sherbivs/service-manager) is running (`npm start`)
2. Note the server's LAN IP address (shown in server startup output)
3. On first app launch, enter: `http://<LAN_IP>:3500`
