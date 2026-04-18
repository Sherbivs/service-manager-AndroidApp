# Tasklist
Last Updated (UTC): 2026-04-18T00:00:00Z
Revision: 1.0

## Active Tasks

- id: SM.INIT.AUTO_START
  title: Make service-manager an auto-starting silent Windows service
  status: READY
  depends_on: []
  priority: P0-CRITICAL
  acceptance_criteria:
    - Service Manager starts automatically on Windows boot/login without a visible console window
    - No manual intervention required after installation
    - Install/uninstall scripts provided
    - Dashboard remains accessible at configured port
    - Managed services auto-start per their autoRestart setting
  notes: |
    First task after governance bootstrap. Options: node-windows (nssm wrapper),
    Windows Scheduled Task with hidden window, or PowerShell service wrapper.

- id: SM.FEAT.MULTI_PROJECT
  title: Add project grouping and filtering to dashboard
  status: PROPOSED
  depends_on: []
  priority: P2-MEDIUM
  acceptance_criteria:
    - Services grouped by project field in dashboard
    - Filter/toggle to show/hide project groups
    - Persisted filter state in localStorage

- id: SM.FEAT.LOG_VIEWER
  title: Add real-time log viewer panel to dashboard
  status: PROPOSED
  depends_on: []
  priority: P2-MEDIUM
  acceptance_criteria:
    - Log panel accessible from dashboard
    - Auto-scrolling with pause capability
    - Filter by service ID
    - Color-coded log levels (stdout vs stderr)

- id: SM.FEAT.SERVICE_EDITOR
  title: Add/edit/remove services from dashboard UI
  status: PROPOSED
  depends_on: []
  priority: P3-LOW
  acceptance_criteria:
    - Form to add new service definitions
    - Edit existing service configuration
    - Delete service (with confirmation)
    - Changes persist to services.json

- id: SM.INFRA.TESTING
  title: Add test framework and basic test coverage
  status: PROPOSED
  depends_on: []
  priority: P2-MEDIUM
  acceptance_criteria:
    - Test runner configured (vitest or jest)
    - Health check function unit tested
    - API endpoint integration tests
    - npm test script wired up
