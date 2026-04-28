# API Integration
**Project:** Service Manager Android App
**Status:** Active
**Last Updated:** 2026-04-28

## Overview
The Android app integrates with the Service Manager REST API using Retrofit + OkHttp.

Integration design goals:
- dynamic base URL resolution from secure local storage
- predictable error mapping into user-facing messages
- support for high-volume archive workflows through pagination and filtering

## Network Stack
- Retrofit interface: app/src/main/java/com/servicemanager/app/data/api/ApiService.kt
- Repository wrapper: app/src/main/java/com/servicemanager/app/data/repository/ServiceRepository.kt
- DI module: app/src/main/java/com/servicemanager/app/di/NetworkModule.kt

Key behavior:
- Retrofit singleton uses a placeholder base URL.
- OkHttp interceptor rewrites scheme/host/port on each request using the saved server URL.
- HTTP logging is disabled for performance-sensitive mobile operation.

## Endpoint Coverage
Core service control:
- GET /api/services
- POST /api/services/:id/start
- POST /api/services/:id/stop
- POST /api/services/:id/restart
- POST /api/services/:id/reset-circuit-breaker

System and live logs:
- GET /api/system
- GET /api/logs?lines=N

Archive search:
- GET /api/services/:id/logs/archive
	- query params: q, level, from, to, limit, offset
- GET /api/logs/archive
	- query params: q, project, level, from, to, limit, offset
- GET /api/logs/projects

## DTO and Response Contract
Archive row model includes:
- id
- service_id
- project
- log_level
- line
- archived_at

Archive envelope includes:
- rows
- total
- limit
- offset

These fields allow the UI to render cross-project context and page through large result sets.

## Error Mapping
Repository-level API failures are normalized into localized messages:
- timeout -> request timeout guidance
- network I/O -> connectivity/settings guidance
- HTTP errors -> server status guidance
- fallback -> unknown error

This keeps ViewModels simple and ensures consistent UX messaging across screens.

## Log UX Scalability Notes
Implemented behavior:
- archive request cancellation before launching a new search
- archive dialog state collection tied to dialog lifecycle
- archive pagination controls (prev/next + page range)
- project and level filtering for global archive search

These controls reduce wasted network calls and keep the archive experience usable as log volume grows.
