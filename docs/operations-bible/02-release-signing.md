# Release Signing
**Project:** Service Manager Android App  
**Last Updated:** 2026-04-24

---

## Overview

Release APKs must be signed before distribution. The build reads signing credentials from a `keystore.properties` file at the project root. This file is **gitignored** and must never be committed.

---

## keystore.properties Format

Create `keystore.properties` in the project root (same level as `app/`):

```properties
storeFile=../release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD
```

> `storeFile` is relative to the `app/` directory. Use `../` to reference a keystore in the project root, or an absolute path.

---

## Creating a Keystore

```bash
keytool -genkey -v \
  -keystore release.keystore \
  -alias service-manager \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

---

## Build Behaviour

| Scenario | Outcome |
|----------|---------|
| `keystore.properties` exists | Release APK signed with release keystore |
| `keystore.properties` absent | Release APK signed with debug keystore (local dev only) |

This allows `./gradlew assembleRelease` to succeed locally without a keystore, while CI always uses a real keystore injected from secrets.

---

## CI/CD (GitHub Actions)

The tag pipeline (`on: push: tags`) injects keystore values via GitHub Secrets:

```yaml
- name: Create keystore.properties
  run: |
    echo "storeFile=${{ secrets.KEYSTORE_PATH }}" > keystore.properties
    echo "storePassword=${{ secrets.KEYSTORE_STORE_PASSWORD }}" >> keystore.properties
    echo "keyAlias=${{ secrets.KEYSTORE_KEY_ALIAS }}" >> keystore.properties
    echo "keyPassword=${{ secrets.KEYSTORE_KEY_PASSWORD }}" >> keystore.properties
```

Required GitHub Secrets:
- `KEYSTORE_PATH` — path to the `.keystore` file (or use a base64-encoded secret + decode step)
- `KEYSTORE_STORE_PASSWORD`
- `KEYSTORE_KEY_ALIAS`
- `KEYSTORE_KEY_PASSWORD`

---

## Verifying a Release APK

```bash
# Build release
./gradlew assembleRelease

# Verify signing
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# Check for exposed sensitive strings (should show obfuscated output)
strings app/build/outputs/apk/release/app-release.apk | grep -i "192.168\|password\|secret"
```
