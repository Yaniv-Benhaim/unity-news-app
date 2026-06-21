# Server Presentation Module

Gradle module: `:features:server:presentation`

This module contains the visible backend console UI.

## What This Module Contains

- `screen/BackendConsoleScreen`, the Compose screen for backend controls.
- `BackendConsoleViewModel`, the state holder for the console.
- `model/BackendConsoleUiState`, the UI state model shown by the console.

## Folder Structure

```text
presentation/
  BackendConsoleViewModel.kt
  model/
    BackendConsoleUiState.kt
  screen/
    BackendConsoleScreen.kt
```

## What This Module Owns

- Showing whether the foreground service is running.
- Letting the operator choose backend scenarios.
- Showing article count and request logs.
- Translating backend runtime state into console UI state.

## Dependency Direction

This module depends on backend `domain`, `data`, and `core:ui` because the console controls runtime scenario/log objects and reuses shared console UI primitives.

It should not own Android service startup or manifest configuration. That belongs in `:app`.

## Useful Commands

```bash
./gradlew :features:server:presentation:assembleDebug
```
