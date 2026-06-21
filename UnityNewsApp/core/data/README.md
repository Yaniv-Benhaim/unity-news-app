# Reader Core Data Module

Gradle module: `:core:data`

This module is reserved for shared data-layer utilities used across reader features.

## What This Module Contains

- Shared data dependencies such as Kotlin serialization, Hilt, and coroutine test tooling.
- `stableStringSetMapCacheKey`, used for deterministic cache keys.
- `supportedRemoteUrlOrNull`, used before rendering remote dataset URLs.

## What Belongs Here

- Generic serialization helpers.
- Shared result/error mapping utilities.
- Common data abstractions that are not specific to the news feature.

## What Does Not Belong Here

- News repository interfaces.
- Article cache tables.
- AIDL-specific news backend code.
- UI code.

## Dependency Direction

Feature data modules may depend on `:core:data` when shared data utilities exist.

This module should not depend on feature modules.
