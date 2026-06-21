# News Domain Module

Gradle module: `:features:news:domain`

This module contains the business-facing contract for the reader's news feature.

## What This Module Contains

- Domain models:
  - `Article`
  - `FilterCriteria`
  - `FilterSpec`
  - `FilterType`
- Repository contract:
  - `NewsRepository`
- Use cases:
  - `ObserveArticlesUseCase`
  - `RefreshArticlesUseCase`
  - `GetFilterSpecsUseCase`

## What This Module Owns

- The language of the news feature.
- The contracts presentation code depends on.
- Use cases that describe what the feature can do.

## Dependency Direction

This module is pure Kotlin and should not depend on Android, Room, AIDL, Retrofit, Compose, or Hilt.

Allowed direction:

```text
presentation -> domain <- data
```

The domain module defines interfaces. The data module implements them.

## Testing Guidance

Use plain JVM unit tests here. Domain tests should be fast and not require Android.

```bash
./gradlew :features:news:domain:test
```
