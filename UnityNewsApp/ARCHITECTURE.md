# Reader Architecture And Scalability Notes

## Role In The Two-App System

`UnityNewsApp` owns the user experience. It renders the feed, lets the user edit filters, sends applied filter criteria to the backend app, and displays the returned article list.

The reader does not own article filtering. Filtering happens in the backend app.

## Module Direction

```text
app
  -> features/news/presentation
  -> features/news/domain
  <- features/news/data
```

The ViewModel depends on use cases. Use cases depend on the `NewsRepository` interface. The data module implements that interface with Room for local cache and AIDL for remote backend calls.

## Data Flow

```text
User edits filters
-> UI stores draft criteria
-> User taps Apply filters
-> ViewModel applies criteria
-> RefreshArticlesUseCase
-> OfflineFirstNewsRepository
-> AidlArticleDataSource
-> backend app filters articles
-> Room cache is updated
-> UI observes cached articles
```

Room is the display source of truth. This lets cached content remain visible if the backend is temporarily missing or returns an error.

## Image Loading

The backend returns image URLs and placeholder colors. The reader uses Coil to load valid HTTP/HTTPS images. The placeholder color is always drawn behind the image, so missing, slow, or broken image URLs degrade gracefully without removing the article.

## Security

The reader binds to a versioned AIDL service. It declares the backend package in `<queries>` and requests the backend signature permission. The backend also validates the calling UID/package/signature at runtime.

## Scalability Path

The assignment does not require pagination, so the current AIDL API returns a full filtered list. That is acceptable for the bundled JSON dataset, but it is not how a production app should handle millions of articles.

For production scale, I would evolve the architecture this way:

- Replace full-list AIDL responses with paged responses.
- Use Paging 3 in the reader.
- Use Room `PagingSource` and `RemoteMediator` for offline-first paging.
- Move the remote implementation from AIDL to an HTTP-backed `RemoteArticleDataSource`.
- Keep presentation/domain mostly stable by swapping repository/data implementations.
- Add cache eviction, TTL, and database indexes.
- Keep image delivery behind a CDN with fixed thumbnail sizes.

The key architectural point is that presentation and domain do not know where articles come from. That makes the future HTTP/server implementation a data-layer replacement instead of a full app rewrite.
