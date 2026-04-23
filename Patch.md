# Patch.md — AI Project Manager Contract
**Project:** Service Manager Android App  
**Repo:** Sherbivs/service-manager-AndroidApp  
**Last Updated:** 2026-04-22

---

## Absolutes (Never Violate)

1. **Never commit secrets.** No API tokens, passwords, server IPs, keystore files, or `.jks` files in source control. Use `EncryptedSharedPreferences` or `keystore.properties` (gitignored).
2. **Never hardcode server URLs.** The Service Manager host is user-configurable. Always read from secure storage.
3. **Never use GlobalScope.** All coroutines must launch from `viewModelScope` or a supervised coroutine scope.
4. **Never skip ROUTER updates.** Every structural file change requires updating the relevant `ROUTER.md` and `ops/ROUTER.yaml` in the same commit.
5. **Never leave `android:debuggable="true"` in release builds.** R8 + no debuggable = required for release.
6. **Task queue discipline.** Only work on tasks listed in `ops/NEXT.yaml`. Mark tasks in-progress before starting, completed immediately after finishing.

---

## Workflow

### Before Starting Any Task
1. Read this file completely.
2. Check `ops/NEXT.yaml` — confirm the task is READY.
3. Read `Prompt.md` for current project state.
4. Check `Tasklist.md` for acceptance criteria and dependencies.

### During Work
- Make minimal, reversible changes. One logical change per commit.
- Follow Kotlin idioms and MVVM patterns (see `AGENTS.md`).
- Keep security invariants (see Security Baseline in `AGENTS.md`).
- Update ROUTER files whenever adding/moving files or directories.

### Before Finishing
1. Update `Prompt.md` with a PATCHSET echo of what changed.
2. Update `ops/NEXT.yaml` — mark the task complete, point to next READY task.
3. Update `Tasklist.md` — move task to DONE, update status of dependents.
4. Commit with message format: `[Task SMA.XXX] Brief summary`

---

## Task ID Format

```
SMA.NNN        — Main feature/fix task
SMA.NNN.QQQ    — Sub-task or quick fix under a main task
```

---

## PATCHSET Echo Template (for Prompt.md updates)

```
PATCHSET SMA.XXX — <task title>
Date: YYYY-MM-DD
Files Changed:
  - path/to/file.kt  (added/modified/deleted)
  - ...
Summary: <one paragraph of what changed and why>
Testing: <how it was verified>
Next: SMA.YYY — <next task title>
```

---

## Recovery Protocol

If a run gets stuck (same error ≥2 times, no progress in 3 steps):
1. **Shrink scope** — attempt only the smallest failing unit
2. **Swap strategy** — try a different implementation approach
3. **Reset context** — clear working memory, re-read AGENTS.md
4. **Escalate** — document the blocker in `Tasklist.md`, ask the user

---

**Document Version:** 1.0 (2026-04-22) — Initial Android bootstrap
