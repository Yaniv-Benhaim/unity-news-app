---
name: android-security-review
description: Use before changing backend permissions, exported services, caller validation, or release build settings.
---

# Android Security Review Skill

Use this skill for backend security-sensitive changes.

## Backend IPC Checklist

- Exported service requires the expected permission.
- Caller signature/package validation still runs.
- Unsupported API versions fail closed.
- Remote exceptions are handled without crashing the service.

## Foreground Service Checklist

- Notification channel is created before foreground start.
- Service lifecycle changes are visible in the console.
- Stop behavior releases long-running work.

## Release Checklist

- R8/minification is enabled for release.
- Keep rules are limited to required Android/Hilt/AIDL entry points.
- Logs avoid secrets and personally identifiable information.
