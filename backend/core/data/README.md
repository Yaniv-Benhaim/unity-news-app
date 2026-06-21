# Backend Core Data Module

Gradle module: `:core:data`

This module is reserved for shared backend data-layer utilities.

## What This Module Contains

- Shared data dependencies such as Kotlin serialization, Hilt, and coroutine test tooling.
- A place for future cross-feature backend data helpers.

## What Belongs Here

- Generic serialization helpers.
- Shared parsing utilities.
- Reusable data abstractions that are not tied to server articles.

## What Does Not Belong Here

- Article repository implementations.
- AIDL contract DTOs.
- Scenario state.
- Console UI code.

## Dependency Direction

Backend feature data modules may depend on `:core:data` when shared helpers exist.

This module should not depend on backend feature modules.
