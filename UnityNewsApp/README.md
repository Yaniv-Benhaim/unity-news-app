# UnityNewsApp

`UnityNewsApp` is the client-facing Android app. It renders a focused news feed, dynamic backend-provided filters, and offline-first cached content.

## Responsibilities

- Bind to the backend app through AIDL API version 2.
- Keep Room as the source of truth for displayed articles.
- Preserve cached content during backend failures.
- Render filter controls from backend `FilterSpec` values.
- Keep domain and presentation independent of whether remote data comes from AIDL today or HTTP later.

## Build And Test

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew :app:assembleRelease
```

## Runtime Setup

Install the backend app first and start its foreground service from the backend console. The UI app declares the backend package visibility and signature permission required for discovery and binding.

If the backend is missing or unavailable, the UI shows a setup screen that opens the backend package listing through Android system intents.

## Key Modules

- `features/news/domain`: article/filter models and repository contract.
- `features/news/data`: Room cache, AIDL client, repository implementation, contract DTOs.
- `features/news/presentation`: ViewModel, UI state, filter controls, news screen.
- `app`: Hilt wiring and the activity shell.

## Company AI Skills

The `company-skills/` folder demonstrates how a company can package repeatable Codex workflows for future developers. These skills cover feature modules, presentation structure, offline-first caching, backend filter contracts, and Android security review.

In a real organization this folder would be published from an internal skill registry. It is included here so reviewers can inspect the operating model directly.
