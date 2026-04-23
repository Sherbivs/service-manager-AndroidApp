# Prompt.md — Service Manager Android App
**Project:** Service Manager Android App  
**Repo:** Sherbivs/service-manager-AndroidApp  
**Timestamp:** 2026-04-22T00:00:01Z

---

## Current State

Project is in **pre-implementation phase**. All meta docs, governance files, and project structure are in place. No application source code has been written yet. Meta docs are now fully aligned with Android architecture and engineering standards.

The Android Gradle project is scaffolded with:
- `app/src/main/AndroidManifest.xml` — INTERNET permission, MainActivity placeholder
- `app/build.gradle` — minSdk 24, targetSdk 34, compileSdk 34, ViewBinding enabled
- `build.gradle` — AGP 8.2.2, Kotlin 1.9.22
- `settings.gradle` — rootProject.name = "Service Manager", includes `:app`

**Next action:** Begin SMA.009 (Hilt DI setup) — the foundation that unblocks SMA.010 (Navigation). Run SMA.001, SMA.007, SMA.011, SMA.012 in parallel after Hilt is in place.

---

## PATCHSET Standards Alignment — Meta Docs Update
Date: 2026-04-22 (Session 2)  
Files Changed:
  - AGENTS.md — expanded with UDF, Hilt DI, Navigation Component, testing pyramid, code quality, 10-item pitfalls list. Version bumped to 1.1
  - .github/copilot-instructions.md — fully rewritten; old Node.js content purged; complete Android standards added (UDF + Hilt + Navigation code examples, testing pyramid, quality gates, 10-step feature checklist)
  - Tasklist.md — expanded from 8 to 13 tasks; added SMA.009 (Hilt), SMA.010 (Navigation), SMA.011 (Testing Infra), SMA.012 (ktlint/detekt), SMA.013 (CI/CD); all ACs updated with Hilt and testing requirements
  - docs/architecture-bible/INDEX.md — full content: layered architecture diagram, UDF flow, sealed UiState pattern, Hilt scoping, Navigation structure, optional Domain layer guidance, design decisions table
  - docs/development-bible/INDEX.md — full content: quick setup, architecture rules table, sealed UiState pattern, testing pyramid, unit/integration test templates, MainDispatcherRule, coverage targets, code quality gate commands, CI/CD pipeline overview
  - ops/NEXT.yaml — version 2; queue updated with SMA.009, SMA.010, SMA.011, SMA.012, SMA.013; recommended execution order documented
  - Prompt.md — this update

Summary: Completed standards-alignment pass informed by Android App Architecture & Engineering Best Practices research. All meta docs now consistently document MVVM + UDF, Hilt DI, Navigation Component, testing pyramid (JUnit/MockK/Turbine/MockWebServer/Espresso), code quality tooling (ktlint/detekt/Lint), and CI/CD patterns. Five new tasks added to backlog.

Testing: Files reviewed for correctness and consistency.

Next: SMA.009 — Dependency Injection / Hilt Setup

---

## PATCHSET Bootstrap — Meta Docs Setup
Date: 2026-04-22  
Files Changed:
  - AGENTS.md  (rewritten — Android-specific agent guide)
  - README.md  (rewritten — Android project overview)
  - ROUTER.md  (rewritten — Android project structure)
  - .github/copilot-instructions.md  (rewritten — Android Copilot rules)
  - Patch.md  (created — AI Project Manager Contract)
  - Tasklist.md  (created — initial READY queue, 8 tasks)
  - Prompt.md  (created — this file)
  - ops/NEXT.yaml  (created — points to SMA.001)
  - ops/ROUTER.yaml  (created)
  - ops/TOUCHMAP.yaml  (created)
  - ops/ROUTER.md  (created)
  - .gitignore  (replaced with Android-specific gitignore)

Summary: Bootstrapped all meta docs for the Android companion app project.

---

## Active Tasks (READY)

**SMA.009** — Dependency Injection / Hilt Setup (P0)  
**SMA.001** — Network Layer Foundation (P0)  
**SMA.007** — Network Security Config (P0)  
**SMA.011** — Testing Infrastructure (P1)  
**SMA.012** — Code Quality Tooling / ktlint + detekt (P1)  
**SMA.013** — GitHub Actions CI/CD (P2)

## Blocked Tasks

**SMA.010** — Navigation Component Setup (blocked on SMA.009)  
**SMA.002** — Server URL Onboarding (blocked on SMA.001, SMA.009, SMA.010)  
**SMA.003** — Services List Screen (blocked on SMA.001, SMA.009, SMA.010)

---

## Known Blockers / Issues

- `app/build.gradle` has `minifyEnabled false` on release — must be set to `true` as part of SMA.008
- No signing config yet — documented as SMA.008 acceptance criteria
- No source code in `app/src/main/java/` yet — all features are pending

---

## Recent Changes Log

| Date | Task | Summary |
|------|------|---------|
| 2026-04-22 | Bootstrap | All meta docs created/rewritten; governance structure in place |
