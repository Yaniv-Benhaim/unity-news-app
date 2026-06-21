---
name: backend-service-change
description: Use when changing backend app service, console, or scenario behavior.
---

# Backend Service Change Skill

Use this skill before changing the backend Android app.

## Architecture Rules

- Backend app must remain independently installable and launchable.
- Backend must not depend on the reader app.
- Filtering runs inside backend domain use cases.
- The visible console should make service state, scenario state, and request logs inspectable.

## Required Tests

- Domain tests for filtering and scenario-independent logic.
- Data tests for JSON parsing and contract DTO parceling.
- Manual verification that the foreground service can start and stop from the console.

## Review Checklist

- AIDL API version stays explicit.
- Caller validation still happens before article/filter responses.
- Scenario controls cannot leak into domain filtering rules.
- Console UI uses `core:ui` for shared operational components.
