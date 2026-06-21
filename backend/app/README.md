# Backend App Module

Gradle module: `:app`

This is the Android application shell for the companion backend app.

## What This Module Contains

- `MainActivity`, the visible backend control console entry point.
- `UnityNewsBackendApplication`, the Hilt application root.
- `NewsBackendService`, the bound AIDL service used by the reader app.
- `NewsBackendForegroundService`, the foreground service that keeps the backend alive.
- App-level theme files and Android application resources.

## What This Module Owns

- Starting and stopping the foreground backend service.
- Exposing the Android service endpoint to the reader app.
- Connecting the visible console UI to backend runtime state.
- Holding Android app configuration, minify settings, manifest entries, and resources.

## Dependency Direction

This module can depend on all backend modules:

- `:core:ui`
- `:core:data`
- `:features:server:domain`
- `:features:server:data`
- `:features:server:presentation`

Feature modules should not depend on `:app`.

## Useful Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
