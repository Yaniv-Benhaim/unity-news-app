# Backend Architecture And Scalability Notes

## Role In The Two-App System

`backend` owns the article dataset and filtering logic. It is an Android app, but conceptually it plays the role of a local server for this exercise.

The backend is independently installable and launchable. It does not depend on the reader app at compile time or runtime.

## Module Direction

```text
app
  -> features/server/presentation
  -> features/server/domain
  <- features/server/data
```

The app module exposes Android services and the visible backend console. The domain module owns article/filter/scenario models and filtering use cases. The data module loads the bundled JSON and owns shared AIDL contract DTOs.

## Backend API Surface

The backend exposes a versioned AIDL bound service:

- `getArticles(request, callback)`
- `getFilterSpecs(callback)`
- `getBackendStatus(callback)`
- `apiVersion`

Filtering runs inside the backend service through `FilterArticlesUseCase`.

## Security

The AIDL service is exported because the reader app must bind to it. Access is restricted in two layers:

- Signature permission on the service declaration.
- Runtime caller validation using UID, package name, and signature match.

This is stronger than relying only on package name checks.

## Scalability Path

The current backend loads a bundled JSON file and filters in memory. That matches the assignment and keeps the code easy to review.

For production scale, this Android backend would likely become a real server. The migration path would be:

- Keep the domain models/use cases as the business contract.
- Replace `AssetArticleRepository` with a database-backed repository.
- Add indexed search/filter queries.
- Return paged article results instead of full lists.
- Add API authentication, rate limiting, tracing, and metrics.
- Serve images through a CDN rather than trusting arbitrary original URLs.

If the backend becomes an HTTP service, the reader app should only need a new data-layer implementation. Presentation and most domain code should remain stable.
