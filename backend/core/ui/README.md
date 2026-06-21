# Backend Core UI Module

Gradle module: `:core:ui`

This module is reserved for shared UI building blocks used by backend screens.

## What This Module Contains

- Shared Compose dependencies.
- `ConsoleSection`, a reusable section layout for console screens.
- `StatusLine`, a reusable label/value row for operational status.

## What Belongs Here

- Generic console UI components.
- Shared loading, empty, status, and card components.
- Styling helpers that are not specific to one backend feature.

## What Does Not Belong Here

- Backend service code.
- Repository or article filtering logic.
- Feature-specific console screens.
- Android application setup.

## Dependency Direction

Backend presentation modules may depend on `:core:ui`.

This module should not depend on backend feature modules.
