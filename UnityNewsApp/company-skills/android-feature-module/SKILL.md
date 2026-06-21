---
name: android-feature-module
description: Use when adding a new Android feature to this codebase.
---

# Android Feature Module Skill

Use this skill before creating or changing a feature module.

## Required Structure

Create feature code under `features/<feature-name>/` with these modules:

- `domain`: pure Kotlin models, repository contracts, and use cases.
- `data`: repository implementations, local/remote data sources, DTO mappers.
- `presentation`: ViewModel, UI state, screens, and feature UI components.

## Dependency Rules

- `presentation` depends on `domain`, `core:ui`, and optionally `core:data`.
- `data` depends on `domain` and optionally `core:data`.
- `domain` must not depend on Android framework, Room, AIDL, Compose, or Hilt.
- The app module wires implementations with Hilt.

## Tests Required

- Domain: use case tests with plain JUnit.
- Data: repository/data-source tests covering success, failure, and cache behavior.
- Presentation: ViewModel tests covering state transitions and user actions.

## Output Checklist

- New module README explains responsibility and dependency direction.
- ViewModel injects use cases, not repositories.
- Repository interface lives in domain.
- Repository implementation lives in data.
- No feature-specific UI components are placed in `core:ui`.
