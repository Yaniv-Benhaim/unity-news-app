# News Presentation Module

Gradle module: `:features:news:presentation`

This module contains the UI and presentation state for the reader's news feature.

## What This Module Contains

- `NewsViewModel`, the state holder for the screen.
- `model/NewsUiState`, the sealed UI state model.
- `model/ArticleArtwork`, UI-only article image mapping.
- `screen/`, route-level screens and screen sections.
- `components/`, feature UI components such as article cards and empty/error states.
- `filters/`, backend-driven filter controls.
- Unit tests for ViewModel state behavior and UI-facing helpers.

## Folder Structure

```text
presentation/
  NewsViewModel.kt
  components/
    ArticleCard.kt
    ArticleImage.kt
    EmptyContent.kt
    ErrorContent.kt
    LoadingContent.kt
    StaleMessage.kt
  filters/
    FilterControls.kt
  model/
    ArticleArtwork.kt
    NewsUiState.kt
  screen/
    ArticleFeed.kt
    BackendSetupScreen.kt
    NewsScreen.kt
```

## What This Module Owns

- Translating domain results into UI state.
- Handling user actions such as refresh and filter changes.
- Rendering backend-driven filters.
- Showing content, empty, error, stale-cache, and backend-missing states.

## Dependency Direction

This module depends on `:features:news:domain`, `:core:ui`, and `:core:data`.

It should inject use cases, not repositories. The ViewModel should not know where data comes from.

```text
NewsViewModel -> use cases -> NewsRepository interface
```

## What Does Not Belong Here

- Room entities or DAOs.
- AIDL service binding.
- Backend DTO parsing.
- Repository implementations.

## Useful Commands

```bash
./gradlew :features:news:presentation:testDebugUnitTest
```
