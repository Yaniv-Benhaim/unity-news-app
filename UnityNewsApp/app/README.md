# Reader App Module

Gradle module: `:app`

This is the Android application shell for the news reader.

## What This Module Contains

- `MainActivity`, the Compose entry point for the reader UI.
- `UnityNewsApplication`, the Hilt application root.
- `NewsAppModule`, the production dependency graph.
- App-level theme files.
- Android application configuration, signing/minify settings, and app manifest resources.

## What This Module Owns

- Starting the reader app.
- Connecting Compose screens to the `NewsViewModel`.
- Wiring production dependencies with Hilt.
- Choosing concrete implementations for interfaces from feature modules.

## Dependency Direction

This module is allowed to depend on all reader modules:

- `:core:ui`
- `:core:data`
- `:features:news:domain`
- `:features:news:data`
- `:features:news:presentation`

Feature modules should not depend on `:app`.

## Important Rule

Keep this module thin. Business rules, caching, backend communication, and UI state logic belong in feature modules.

## Useful Commands

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```
