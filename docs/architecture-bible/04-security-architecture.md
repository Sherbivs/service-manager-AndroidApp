# Security Architecture
**Project:** Service Manager Android App
**Status:** Active
**Last Updated:** 2026-05-17

## Security Objectives
- Prevent secret exposure in source, logs, and local storage.
- Restrict network behavior to intended environments.
- Preserve production hardening through build and signing controls.

## MASVS-Oriented Controls
1. Storage (MASVS M2)
- Network settings (scheme/host/port and timeouts) and sensitive app settings are stored using Secure DataStore + Tink.
- Encryption keys are managed through Android Keystore-backed mechanisms.

2. Communication (MASVS M3)
- HTTPS is default expectation.
- LAN cleartext is allowed only via explicit domain exceptions in network_security_config.xml.
- Global cleartext opt-in is not used.

3. Platform and Build Hardening (MASVS M1/M9)
- Release build: debuggable false, minify enabled, resource shrinking enabled.
- Obfuscation and optimization are enforced with R8 rules.
- Signing material is externalized from source control.

## Secret Handling Rules
- Never hardcode credentials, tokens, or private endpoints in code.
- Never log sensitive configuration values in release paths.
- Keep keystore files and keystore.properties outside version control.

## Network Security Model
The app uses a deny-by-default cleartext posture and only permits LAN endpoints required for development and internal deployment scenarios.

Current canonical LAN references for ops/audit context:
- Service Manager API: http://sensaimanager.drip:3500 (LAN IP: 192.168.23.106)
- Shopify dev services: http://tcb.drip and http://blt.drip

## Operational Security Practices
- Validate server reachability from Settings before adopting URL changes.
- Surface user-facing error guidance without exposing internal exception details.
- Keep dependency updates routine to reduce known-vulnerability exposure windows.

## Verification Checklist
- Lint/analysis gates pass without security regressions.
- No secrets committed in code or resources.
- Manifest exported flags are least-privilege.
- Network config remains scoped to explicit allowed hosts.
