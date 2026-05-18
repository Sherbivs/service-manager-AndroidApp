# ROUTER
Title: Service Manager AndroidApp ??? Root
Purpose: Native Android companion app for the Service Manager dashboard. Connects to the Node.js API over LAN.
Owned Globs: *
Last Updated (UTC): 2026-05-17T00:00:00Z

Notes:
- Root meta-router for the Android app project.
- Language: Kotlin. Build system: Gradle. Architecture: MVVM.
- All secrets stored in Secure DataStore + Tink / Android Keystore.
- No server-side code in this repo; API server lives at Sherbivs/service-manager.

Areas:
- app/build.gradle        ??? App module config: namespace, SDK versions, dependencies, signing
- build.gradle            ??? Root Gradle config: plugin versions
- settings.gradle         ??? Project name, included modules
- gradle.properties       ??? JVM args, AndroidX flags
- AGENTS.md               ??? Agent operations guide (START HERE for AI agents)
- Patch.md                ??? AI Project Manager Contract
- Prompt.md               ??? Current state, PATCHSET echo
- Tasklist.md             ??? Task backlog with READY queue
- app/                    ??? Android application module (all source code)
- ops/                    ??? Governance surface (NEXT pointer, routing, task metadata)
- docs/                   ??? Bible documentation (architecture, operations, development)
- .github/                ??? Copilot/AI agent instructions

Subrouters:
- app/ROUTER.md           ??? Android app module (source, res, manifest)
- ops/ROUTER.md           ??? Ops & Governance
- docs/ROUTER.md          ??? Documentation library
- .github/ROUTER.md       ??? AI agent configuration

Environment Baseline:
- Service Manager API: http://sensaimanager.drip:3500 (LAN IP: 192.168.23.106)
- Shopify dev services: http://tcb.drip and http://blt.drip
## Tips
- Use this router as the first navigation anchor before editing any subsystem.
- Keep parent and child routers synchronized whenever structure or file purpose changes.
- Verify work is tied to a READY task and scoped by current governance files.

## Next Steps
1. Identify target subsystem from this root map and open the child router.
2. Confirm task readiness and acceptance criteria before implementation.
3. After changes, sync routers and task metadata in the same change set.

## Troubleshooting
- If ownership is unclear, resolve with parent and child router chain plus ops routing map.
- If runtime behavior conflicts with docs, trust evidence and update docs immediately.
- If scope expands unexpectedly, record it in task metadata before continuing.