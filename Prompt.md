# Prompt.md
Timestamp (UTC): 2026-04-18T00:00:00Z
Revision: 1.0

**Cross-References**:
- See `Patch.md` for contract rules
- See `Tasklist.md` for active backlog and current tasks
- See `ops/NEXT.yaml` for current task pointer

## Purpose & Scope
Canonical brief for automation: captures current mission, guardrails, and truth sources so every run starts aligned.

## Activation Checklist
1. Ensure Patch.md, Tasklist.md, Prompt.md, and ops/NEXT.yaml open cleanly.
2. Read ops/NEXT.yaml and confirm the READY queue in Tasklist.md matches.
3. Review the PATCHSET echo for context on outstanding work.
4. If router manifests diverge from working intent, update them before editing.

## Last Repo Changes
- **2026-04-18:** Bootstrapped meta-router system, Patch contract, Bible documentation, AI instructions, AGENTS.md, and governance surface (ops/). Modeled after SensaiOS patterns, tailored for a lightweight Node.js service manager.

## PATCHSET Echo
<<<PATCHSET START>>>
Header: [Meta] Bootstrap governance system

Summary: Created meta-router, patch system, bible-docs, AI instructions, AGENTS.md, masterroutetable, Tasklist, and Prompt for Service Manager. Governance surface (ops/) established with NEXT pointer, ROUTER.yaml, and TOUCHMAP.

TasklistDiff: true
Tests: N/A (no test framework yet)
Contracts: ON
READY pointer: SM.INIT.AUTO_START
<<<PATCHSET END>>>
