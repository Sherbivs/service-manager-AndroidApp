# System Overview
**Project:** Service Manager Android App
**Status:** Active
**Last Updated:** 2026-04-28

## Mission
Service Manager Android App is a native operational companion for the Service Manager server. It allows operators to monitor service state, inspect logs, and execute service actions from a mobile device on the same network.

## Product Boundaries
- This app is a client only. It does not host or run services.
- Service orchestration remains in the Service Manager backend.
- Android focuses on usability, responsiveness, and secure server access configuration.

## High-Level Architecture
```
Android App (Kotlin, MVVM + UDF)
	|  Retrofit + OkHttp
	v
Service Manager API (Node.js / Express)
	|  start/stop/restart + logs + system info
	v
Managed local development services
```

## Core Functional Domains
1. Services domain
- View service inventory and status.
- Trigger Start, Stop, Restart, and circuit-breaker reset actions.

2. Logs domain
- Load recent global logs.
- Perform archive search by service or globally.
- Apply level/project filters and page through archive results.

3. System domain
- Display server/system telemetry and connectivity context.

4. Settings domain
- Manage server base URL securely.
- Validate connectivity before saving changes.

## Runtime Data Flow
```
User input
	-> Fragment event
	-> ViewModel action
	-> Repository call
	-> ApiService request
	-> Response mapping (Result<T>)
	-> UiState update (StateFlow)
	-> Fragment render
```

## Non-Functional Priorities
- Reliability: predictable feedback for every action.
- Scalability: archive search cancellation + pagination for large datasets.
- Security: secure local storage and controlled cleartext policy for LAN use.
- Maintainability: strict layer boundaries and testability.
