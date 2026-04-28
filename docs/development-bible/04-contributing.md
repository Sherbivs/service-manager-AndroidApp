# Contributing Guide
**Project:** Service Manager Android App
**Status:** Active
**Last Updated:** 2026-04-28

## Contribution Contract
All changes should be task-driven, test-backed, and documented.

Required references before implementation:
- Patch.md
- Prompt.md
- Tasklist.md
- ops/NEXT.yaml
- AGENTS.md

## Task and Branch Conventions
Task IDs use SMA.NNN format.

Recommended branch naming:
- feature/SMA-015-log-scalability
- fix/SMA-016-doc-sync
- chore/SMA-012-tooling

## Commit Message Format
```
[Task SMA.XXX] Brief summary

Why: reason for change
Changes: major code or doc updates
Testing: verification performed
```

## Definition of Done
A task is done only when all are true:
1. Acceptance criteria in Tasklist.md are satisfied.
2. Relevant tests are added or updated.
3. Quality gates pass locally.
4. Docs are updated for behavior, architecture, or workflow changes.
5. Routing metadata is updated when structure or ownership changes.

## Required Quality Gates
Run before creating or updating a PR:
```bash
./gradlew lint
./gradlew ktlintCheck
./gradlew detekt
./gradlew test
```

If formatting issues exist:
```bash
./gradlew ktlintFormat
```

## Code Review Expectations
- Keep changes scoped and reversible.
- Avoid unrelated refactors in feature patches.
- Highlight user-visible behavior changes in PR description.
- Call out risk areas and follow-up tasks explicitly.

## Router and Documentation Rules
Update routing artifacts in the same patch when adding directories/files or changing ownership/purpose:
- parent and child ROUTER.md files
- ops/ROUTER.yaml when structural routing changes

Update bible docs when architecture, operations, or development workflow changes.

## Security and Safety Baseline
- No secrets in repository.
- No hardcoded production credentials/endpoints.
- Preserve least-privilege manifest and network policies.

## Practical PR Checklist
- [ ] Task ID referenced in branch/commit/PR
- [ ] Tests added or updated
- [ ] Local gates pass
- [ ] Docs and routers updated where required
- [ ] Prompt.md and ops/NEXT.yaml reflect final status
