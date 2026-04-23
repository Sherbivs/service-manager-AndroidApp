# Prompt.md — Service Manager Android App
**Project:** Service Manager Android App  
**Repo:** Sherbivs/service-manager-AndroidApp  
**Timestamp:** 2026-04-22T00:00:00Z

---

## Current State

Project is in **bootstrap phase**. All meta docs, governance files, and project structure are in place. No application source code has been written yet.

The Android Gradle project is scaffolded with:
- `app/src/main/AndroidManifest.xml` — INTERNET permission, MainActivity placeholder
- `app/build.gradle` — minSdk 24, targetSdk 34, compileSdk 34, ViewBinding enabled
- `build.gradle` — AGP 8.2.2, Kotlin 1.9.22
- `settings.gradle` — rootProject.name = "Service Manager", includes `:app`

**Next action:** Begin SMA.001 (Network Layer) and SMA.007 (Network Security Config) — both are READY.

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

Summary: Bootstrapped all meta docs for the Android companion app project. The Node.js service-manager content was present from a repo separation operation; all files have been rewritten for the Android project. Governance structure mirrors the service-manager repo pattern with Android-specific content throughout.  

Testing: Files verified by reading back key sections.  

Next: SMA.001 — Network Layer Foundation

---

## Active Task

**SMA.001** — Network Layer Foundation  
**SMA.007** — Network Security Config (LAN HTTP support)

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
