# AI PROJECT MANAGER CONTRACT
Last Updated (UTC): 2026-04-18T00:00:00Z
Revision: 1.0

This repository uses **Patch.md** as the working agreement between humans and automation.
Read this file completely before beginning any task. Follow it literally.

**Cross-References**:
- See `Prompt.md` for current timestamp, recent changes, PATCHSET echo template
- See `Tasklist.md` for backlog (READY queue, dependencies, acceptance criteria)
- See `ops/NEXT.yaml` for task pointer discipline (must point to READY task)
- See `ops/TOUCHMAP.yaml` for file scope boundaries per task

## Absolutes (apply in every mode)
- Never rename or delete canonical meta/routing files (`Patch.md`, `Tasklist.md`, `Prompt.md`, `ops/ROUTER.yaml`, `ops/NEXT.yaml`, `masterroutetable.md`, and any `ROUTER.md`).
- Keep every change reversible and minimal.
- Do not add unnecessary dependencies without justification.
- `services.json` is user configuration — never overwrite user service entries without explicit permission.
- The repo must always exit a run with a READY pointer, a clean PATCHSET echo, and accurate history.

## Preflight
1. Read `ops/NEXT.yaml` to learn the queued task(s) and `files_hint`.
2. Read `Tasklist.md` and execute **only** tasks marked `status: READY` with dependencies satisfied.
3. Read `ops/TOUCHMAP.yaml` to scope edits to the listed paths.
4. Confirm `Patch.md`, `Prompt.md`, and `Tasklist.md` are readable. If any are missing or corrupt, stop and request refreshed copies.
5. If no READY tasks exist, promote the oldest eligible PROPOSED whose dependencies are DONE.

## Operating Loop
1. **Plan & Execute**
   - Select up to 3 main tasks from READY queue (respect dependency order).
   - Quick/hot tasks (bug fixes, docs, < 30min) can be added as a 4th concurrent task.
   - Implement changes, run tests when available.
2. **Update State**
   - Mark completed tasks `DONE` in `Tasklist.md` with concise outcome notes.
   - Update `ops/NEXT.yaml` main_tasks queue with updated timestamp and rationale.
   - Extend `ops/TOUCHMAP.yaml` if you touched new paths.
   - Refresh `Prompt.md` (Timestamp, Revision, Last Repo Changes, PATCHSET Echo).
3. **Meta-System Maintenance**
   - **Router Sync:** If code changes touched new directories or modified file purposes → Update parent/child `ROUTER.md`
   - **Bible Integration:** If architectural decisions or operational procedures established → Document in appropriate Bible section
   - **Tasklist Cleanup:** If >20 DONE tasks accumulate → Archive old tasks

## Postflight
- Refuse to exit if Tasklist.md lists no READY tasks; auto-promote as needed.
- Refuse to exit if `ops/NEXT.yaml:main_tasks` is empty; populate at least one task before finishing.
- Record the final active tasks inside the PATCHSET Echo.

## Prompt Echo Template
Keep the PATCHSET Echo between the markers in `Prompt.md`.
Include: a concise summary sentence, `TasklistDiff true|false`, test results, and the READY pointer rationale.

## Tasklist Discipline
- Update headers (`Last Updated`, `Revision`) whenever Tasklist.md changes.
- Add acceptance criteria for new tasks; include outcome notes when marking DONE.
- Never delete tasks with active dependencies.

## Commit Message Guidelines
- Use the format `[Task <ID>] Concise summary` when closing a Tasklist item.
- Include a short "Why / Changes / Testing" body when additional context helps reviewers.

## Hotfix Loop
- Trigger with `ACTIVATE: HOTFIX ENGINE: <short note>`.
- Restrict edits to files listed in the hotfix request + `ops/NEXT.yaml` + `Tasklist.md`.
- Restore `ops/NEXT.yaml` main_tasks queue before exiting.

## Patch Clipboard
_Paste the exact patchset below this heading before you begin editing so humans can audit the plan._
_After the run, clear or archive the clipboard._
