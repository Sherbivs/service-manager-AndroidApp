# ARCHITECTURE BIBLE — INDEX

**Centralized System Architecture Documentation**
**Created:** 2026-04-18 | **Last Updated:** 2026-04-18 | **Status:** Active

---

## Purpose

Single source of truth for Service Manager architectural design, component relationships, and technical decisions.

---

## Architecture Sections

| Section | File | Topics |
|---------|------|--------|
| **1. System Overview** | [01-system-overview.md](01-system-overview.md) | Architecture snapshot, technology stack, project structure |
| **2. Server Architecture** | [02-server-architecture.md](02-server-architecture.md) | Express app, process registry, health checks, auto-restart engine |
| **3. Dashboard Architecture** | [03-dashboard-architecture.md](03-dashboard-architecture.md) | SPA design, auto-refresh, API integration, responsive layout |
| **4. API Design** | [04-api-design.md](04-api-design.md) | REST endpoints, request/response contracts, error handling |
| **5. Configuration** | [05-configuration.md](05-configuration.md) | services.json schema, hot-reload, validation |
| **6. Router System** | [06-router-system.md](06-router-system.md) | Meta-router design, routing discipline, navigation patterns |

---

## Quick Navigation

- **How does the server work?** → § 2
- **How does the dashboard render?** → § 3
- **What API endpoints exist?** → § 4
- **How do I add a service?** → § 5
- **How does the router system work?** → § 6

---

## Cross-References

- **[OPERATIONS-BIBLE](../operations-bible/INDEX.md)** — Installation, deployment, monitoring
- **[DEVELOPMENT-BIBLE](../development-bible/INDEX.md)** — Local setup, testing, contributing
- **[AGENTS.md](../../AGENTS.md)** — Agent operations guide
- **[README.md](../../README.md)** — Getting started
