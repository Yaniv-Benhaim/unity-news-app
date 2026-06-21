---
name: android-security-review
description: Use before changing manifests, IPC, exported components, permissions, or release build settings.
---

# Android Security Review Skill

Use this skill for security-sensitive Android changes.

## Manifest Checklist

- Every exported component has a reason.
- Bound services require explicit action/package and permission checks.
- Package visibility is as narrow as possible.
- Foreground service permissions match actual service behavior.

## IPC Checklist

- Caller identity is validated before serving data.
- AIDL methods return structured success/error callbacks.
- Unknown API versions fail closed.
- Request logs do not include secrets.

## Release Hardening Checklist

- R8/minification is enabled for release builds.
- Keep rules are narrow and justified.
- Debug-only tooling is not shipped in release.
- Signing and package IDs are documented.
