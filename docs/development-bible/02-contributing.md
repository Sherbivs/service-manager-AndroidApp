# § 2 — Contributing

## Code Conventions

- **No unnecessary dependencies** — This project has one dependency (Express). Keep it that way unless there's strong justification.
- **Vanilla JS** — No TypeScript, no JSX, no bundler. Files are served directly.
- **Comments** — Use the section header pattern from `server.js` for logical groupings.
- **Logging** — All log output goes through the `log()` function in `server.js`.

## Router Discipline

Every code change MUST update routers in the same commit:
1. Update parent `ROUTER.md` if adding files to an existing directory
2. Create child `ROUTER.md` if creating a new subdirectory
3. Update `masterroutetable.md` if adding new directories

## Commit Messages

```
[Task SM.XXX.YYY] Brief summary of change

Why: Explain the business/technical reason
Changes: List major modifications
Testing: How it was verified
```

## Task Workflow

1. Check `ops/NEXT.yaml` for current task pointer
2. Only work on tasks marked `READY` in `Tasklist.md`
3. Update `Tasklist.md` when completing tasks (mark DONE with outcome notes)
4. Update `Prompt.md` with changes summary
5. Ensure `ops/NEXT.yaml` points to a READY task before finishing
