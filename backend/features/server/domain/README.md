# Server Domain Module

Gradle module: `:features:server:domain`

This module contains the backend feature's business models and use cases.

## What This Module Contains

- Domain models:
  - `Article`
  - `FilterCriteria`
  - `FilterSpec`
  - `ServerScenario`
- Repository contract:
  - `ArticleRepository`
- Use cases:
  - `FilterArticlesUseCase`
  - `GetFilterSpecsUseCase`

## What This Module Owns

- Backend business rules.
- Article filtering rules.
- Filter metadata rules.
- Scenario names used by the backend console and service.

## Dependency Direction

This module is pure Kotlin and should stay independent from Android, AIDL, assets, Compose, and Hilt.

Allowed direction:

```text
presentation -> domain <- data
```

The domain module defines what the backend can do. The data module decides how to load data.

## Useful Commands

```bash
./gradlew :features:server:domain:test
```
