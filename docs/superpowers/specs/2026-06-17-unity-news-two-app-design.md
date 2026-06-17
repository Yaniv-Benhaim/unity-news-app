# Unity News Two-App Architecture Design

Date: 2026-06-17

## Goal

Build two independently installable Android applications for the Unity Android Tech Lead take-home:

- `UnityNewsApp`: the user-facing news reader.
- `backend`: an Android application that conceptually plays the backend/server role.

The submission should optimize for the assignment evaluation lens: architecture, security, and scalability. UI polish is useful only where it clarifies behavior; it is not the main scoring axis.

## Assignment Constraints

- The backend app owns the bundled article dataset.
- The backend app serves data from local bundled JSON. It must not fetch the Gist at runtime.
- The backend app owns and executes filter logic.
- The UI app owns the news list experience and filter controls.
- The UI app must request filtered articles from the backend app.
- The two apps must have separate application IDs and be installable/launchable independently.
- Required filters: title text and rating, applied together.
- Future filter types should not require a structural rewrite of the UI app.
- Article detail screen, login, user accounts, and persisted per-user preferences are out of scope.
- Tests are expected.
- An AI tooling write-up is required.

## Chosen Architecture

Use two separate Android Gradle projects in one repository:

- `UnityNewsApp/`
- `backend/`

Do not merge them into a single multi-app monorepo. The separation is intentional: the UI app and backend app should behave like separately owned systems that communicate only through a stable inter-app contract.

The communication mechanism is Android-native IPC using an AIDL bound service. This choice fits the assignment better than a local HTTP server because the backend is explicitly still an Android app, and the evaluation includes security. The design still preserves a clean migration path to a real remote backend: only the UI app data-source implementation should need to change.

## Project Structure

Each app uses the same scalable module shape.

### UI App

```text
UnityNewsApp/
  app/
  core/ui/
  core/data/
  features/news/data/
  features/news/domain/
  features/news/presentation/
```

### Backend App

```text
backend/
  app/
  core/ui/
  core/data/
  features/server/data/
  features/server/domain/
  features/server/presentation/
```

### Dependency Rules

```text
app -> presentation -> domain
app -> data
data -> domain
presentation -> domain
domain -> no Android framework dependency where practical
```

- `core/ui`: reusable Compose components and app design primitives.
- `core/data`: infrastructure primitives such as dispatchers, result types, clocks, and test helpers.
- `domain`: business contracts, use cases, and entities.
- `data`: repository implementations, local/remote data sources, mappers, persistence, and IPC adapters.
- `presentation`: ViewModels, UI state, reducers, and Compose screens.

Hilt wires interfaces to implementations. Tests replace data sources and repositories with fakes without changing domain or presentation.

## UI App Design

The UI app is a focused news reader, not a dashboard.

Primary screen:

- News list.
- Title text filter.
- Dynamic rating filter.
- Apply/Save action that applies all selected filters together.
- Image rendering with visible placeholder color from `placeholderColor` until image load completes.
- Clear states for loading, content, empty result, stale cached content, backend unavailable, unauthorized, incompatible backend, and backend missing.

The UI app does not implement backend filtering. It builds typed filter criteria, sends them through the IPC contract, and renders the returned articles.

### UI Domain

```text
Article
FilterSpec
FilterCriteria
NewsRepository
ObserveArticlesUseCase
RefreshArticlesUseCase
ApplyFiltersUseCase
```

### UI Data

```text
OfflineFirstNewsRepository : NewsRepository
RoomArticleLocalDataSource
RemoteArticleDataSource
AidlArticleDataSource : RemoteArticleDataSource
ArticleDto / ArticleEntity / domain mappers
```

The key abstraction is `RemoteArticleDataSource`. Today it is implemented by `AidlArticleDataSource`. If the backend later becomes a real server, the replacement is `HttpArticleDataSource`. Domain and presentation should not change.

## Offline-First Cache

The UI app uses Room as the local source of truth.

Core rule:

```text
UI renders from Room.
Backend refresh updates Room.
Backend failure never destroys cached content.
```

Room tables:

```text
ArticleEntity
  id
  title
  description
  imageUrl
  rating
  placeholderRed
  placeholderGreen
  placeholderBlue
  lastFetchedAt

CachedQueryEntity
  criteriaHash
  articleIds
  lastSuccessfulRefreshAt
  staleReason

CacheMetadataEntity
  key
  value
  updatedAt
```

The cache is keyed by applied criteria so the app can show the last successful result for a filter set if the backend is unavailable. Refreshes must update cache state transactionally.

## Dynamic Filters

The backend exposes filter specifications through AIDL. The UI renders controls from these specs.

Initial specs:

- `title`: text input.
- `rating`: selectable values derived from the bundled dataset, not hardcoded.

Adding a new filter later should require:

- backend adds a new `FilterSpec`;
- backend filter engine supports the new criteria;
- UI renders the filter if it supports that control type;
- no structural rewrite of UI presentation/domain.

Unsupported filter types must be shown as unavailable with a clear defensive path, not crash the UI.

## Backend App Design

The backend app is independently launchable and visibly demonstrates that it owns data, filtering, runtime state, and security.

Main parts:

```text
BackendConsoleScreen
BackendConsoleViewModel
NewsBackendForegroundService
NewsBackendService
FilterArticlesUseCase
GetFilterSpecsUseCase
ArticleRepository
AssetArticleRepository
ServerScenario
RequestLogStore
```

The backend app bundles `articles.json` under `backend/app/src/main/assets/articles.json`. The Gist is only the source file used to populate that local asset.

Filtering rules:

- empty criteria returns all articles;
- title filter is case-insensitive contains;
- rating filter supports one or more selected ratings;
- title and rating are applied together;
- rating filter options are derived from data;
- backend returns typed errors for security/version/scenario failures.

## Backend Operator Console

The backend app has a visible operator console, because it makes the architecture reviewable.

Console shows:

- service running/stopped;
- foreground service status;
- current scenario mode;
- article count;
- available filter specs;
- last client package/UID if available;
- request logs;
- last request duration/result count;
- security status: signature permission and caller validation enabled.

The console lets the reviewer:

- start backend service;
- stop backend service;
- change scenario mode;
- clear request logs.

## Foreground Service

The backend server/runtime is user-started from the backend console.

The foreground service:

- show a persistent notification while running;
- expose a stop action in the notification;
- be stoppable from the backend console;
- own or coordinate the backend runtime state needed by the AIDL service.

The README explains that this is a local backend simulation for the exercise, not an attempt to create an unrestricted always-on daemon. The implementation respects current Android foreground-service restrictions.

## Fault Simulation

Fault modes are included from day one:

- `Normal`
- `Slow`
- `Empty`
- `ServerError`
- `Unauthorized`

These modes allow the reviewer to verify resilience:

- UI loading state under slow response;
- empty result state;
- cached content during backend failure;
- unauthorized/incompatible backend messaging;
- request logs in the backend console.

## IPC Contract

Use duplicated AIDL contract files in both projects and treat them as a versioned external API.

Contract shape:

```text
INewsBackendService
  int getApiVersion()
  void getFilterSpecs(IFilterSpecsCallback callback)
  void getArticles(ArticleFilterRequest request, IArticlesCallback callback)
  void getBackendStatus(IBackendStatusCallback callback)
```

Use asynchronous callbacks to avoid blocking Binder threads and to support slow-response simulation cleanly.

DTOs:

```text
ArticleDto
  id
  title
  description
  imageUrl
  rating
  placeholderRed
  placeholderGreen
  placeholderBlue

ArticleFilterRequest
  titleQuery
  ratingValues
  requestId

FilterSpecDto
  key
  label
  type
  options

BackendStatusDto
  isRunning
  scenario
  articleCount
```

Add an API version check. If the backend version is unsupported, the UI app must show a clear incompatible-backend state.

## Contract Drift Verification

Because AIDL files are duplicated, add a repo-level verification script or Gradle task:

```text
scripts/verify-aidl-contracts.sh
```

It compares:

```text
UnityNewsApp/app/src/main/aidl/com/unitynews/contract/*
backend/app/src/main/aidl/com/unitynews/contract/*
```

Local verification and CI-style instructions must fail if contracts drift.

In production, this contract would likely become a small versioned API artifact, similar in spirit to generated clients from OpenAPI/protobuf. For the assignment, duplication plus drift verification keeps the projects separate while showing contract discipline.

## Security Design

No Android APK can be made impossible to decompile. The correct security posture is layered hardening plus honest threat modeling.

Security requirements:

- no real secrets embedded in either APK;
- backend service exported intentionally and protected by a custom signature-level permission;
- UI app binds with explicit `ComponentName`;
- backend validates caller UID/package and signing certificate before serving data;
- backend returns typed unauthorized errors;
- request logs avoid sensitive data;
- release builds enable R8 minification and resource shrinking;
- keep rules are narrow and specific;
- no broad `-keep com.example.** { *; }` rules;
- release build verification is documented.

The README explains:

```text
Obfuscation raises reverse-engineering cost, but access control is enforced by Android IPC permissions, explicit binding, signature validation, and caller checks. No client-side secret is treated as secure.
```

## Backend Setup Onboarding

Add a reviewer convenience flow in the UI app.

If the backend is not detected:

- show a clean setup screen;
- explain that a companion backend APK is required;
- offer Install Backend, Open Backend, and Retry actions;
- use Android's system-approved package installation flow for the bundled backend APK artifact;
- never silently install;
- never silently start privileged behavior;
- after install, launch the backend console;
- user starts the foreground service;
- UI app retries binding.

This feature is a convenience layer, not the core architecture. The normal submission still includes two independently installable APKs and README installation steps.

## Testing Strategy

Tests are part of the architecture signal.

### Backend Tests

- JSON parsing test.
- filter spec generation test.
- title filtering test.
- rating filtering test.
- combined title + rating filtering test.
- empty criteria test.
- empty result test.
- scenario behavior tests.
- caller/security validation tests for package allowlist and signing-certificate mismatch behavior.

### UI Data Tests

- repository observes cached data;
- refresh writes articles transactionally;
- backend failure preserves cached content;
- stale metadata updates correctly;
- filtered results are cached by criteria hash;
- unsupported backend version maps to UI state;
- fake AIDL/remote data source can be swapped for tests.

### UI Presentation Tests

- filter specs map to filter controls;
- changing controls builds expected criteria;
- Apply/Save calls the expected use case;
- ViewModel emits loading/content/empty/stale/error states;
- backend missing state routes to onboarding.

### Contract Tests

- AIDL contract drift check.
- API version compatibility behavior.

## Delivery And Reviewer Experience

Top-level README:

- assignment summary;
- project layout;
- why AIDL over local HTTP;
- how to build both apps;
- how to install both APKs;
- how to run the demo;
- how to run tests;
- security notes;
- scalability/migration note;
- AI tooling write-up.

Per-app READMEs:

- architecture;
- module map;
- build/run steps;
- app-specific behavior.

Reviewer demo script:

```text
1. Install backend APK and UI APK.
2. Open backend app.
3. Start backend foreground service.
4. Open UI app.
5. Apply title/rating filters.
6. Watch backend request logs update.
7. Switch backend to Slow / Empty / ServerError / Unauthorized.
8. Return to UI app and observe loading/empty/stale/error behavior.
9. Stop backend service and show cached content remains available.
10. Optional: uninstall backend and show UI onboarding/setup flow.
```

Verification commands:

```text
./UnityNewsApp/gradlew testDebugUnitTest
./UnityNewsApp/gradlew assembleDebug
./backend/gradlew testDebugUnitTest
./backend/gradlew assembleDebug
./scripts/verify-aidl-contracts.sh
```

Release checks:

```text
./UnityNewsApp/gradlew assembleRelease
./backend/gradlew assembleRelease
```

## AI Tooling Write-Up Requirements

Include a short honest write-up covering:

- AI tools used and roughly how often;
- where AI accelerated work;
- where AI suggested something wrong or off-architecture;
- how human review corrected the direction;
- what would be done differently next time.

Specific examples to include:

- AI helped diagnose the AndroidX Core `compileSdk` mismatch.
- AI initially considered runtime Gist fetch and local HTTP server, then the assignment was reread and the design corrected to bundled JSON plus Android-native AIDL IPC.
- Human decisions drove the final architecture: separate projects, AIDL, strict security posture, offline-first cache, backend-owned dynamic filtering, and setup onboarding.

## Out Of Scope

- Login or user accounts.
- Per-user persisted preferences.
- Article detail screen.
- Runtime network fetching of the article source.
- Pagination, unless explicitly added later as a separate demonstration.
- Silent backend installation.
- Silent privileged/background start from the UI app.
- Claiming APKs are impossible to reverse engineer.

## Implementation Decisions To Carry Into The Plan

- Use Coil for image loading because it has first-class Compose support and can render a color placeholder while the remote image loads.
- Use Room entities named `ArticleEntity`, `CachedQueryEntity`, and `CacheMetadataEntity`.
- Put Hilt bindings in each module close to the implementation they bind, with app-level composition in each `app` module.
- Implement backend APK setup as a guided system-installer flow. The implementation plan must define how the backend APK artifact is produced and copied into the UI app's installable assets without making the two projects compile-time dependent.
