# News Data Module

Gradle module: `:features:news:data`

This module implements the reader's data layer for the news feature.

## What This Module Contains

- `OfflineFirstNewsRepository`, the implementation of `NewsRepository`.
- Local cache contracts and Room implementation:
  - `NewsLocalDataSource`
  - `RoomNewsLocalDataSource`
  - `NewsDao`
  - `NewsDatabase`
  - `ArticleEntity`
  - `CachedQueryEntity`
- Remote data contract and AIDL implementation:
  - `RemoteArticleDataSource`
  - `AidlArticleDataSource`
  - `BackendConnection`
  - `BackendAvailabilityChecker`
- Shared AIDL transport DTOs and `.aidl` files.
- Unit tests for cache behavior, IPC mapping, and backend availability.

## What This Module Owns

- Offline-first behavior.
- Mapping between domain models, Room entities, and AIDL DTOs.
- Backend service binding.
- Cache keys for dynamic filter criteria.

## Data Flow

```text
ViewModel
  -> UseCase
  -> NewsRepository
  -> Local cache for reads
  -> Backend AIDL service for refreshes
  -> Local cache updated after successful refresh
```

## Important Rule

Presentation code should not know this module uses Room or AIDL. If the backend becomes a real HTTP server, add a new `RemoteArticleDataSource` or repository implementation here and keep domain/presentation unchanged.

## Useful Commands

```bash
./gradlew :features:news:data:testDebugUnitTest
```
