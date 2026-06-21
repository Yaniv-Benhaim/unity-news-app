# Reader Core UI Module

Gradle module: `:core:ui`

This module is reserved for shared UI building blocks used across reader features.

## What This Module Contains

- Shared Compose dependencies.
- `RemoteImageBox`, a reusable remote-image surface with a stable placeholder color.
- Reusable styling helpers that are not specific to the news feature.

## What Belongs Here

- Generic buttons, empty states, loading states, cards, icons, or spacing helpers.
- UI components that can be used by multiple features.
- Visual utilities that do not know about article, backend, or repository concepts.

## What Does Not Belong Here

- Feature-specific screens such as `NewsScreen`.
- ViewModels.
- Repository or network/cache code.
- Android application setup.

## Dependency Direction

Feature presentation modules may depend on `:core:ui`.

This module should stay independent from feature modules.
