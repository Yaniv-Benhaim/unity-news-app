# Server Data Module

Gradle module: `:features:server:data`

This module implements backend data behavior and owns the shared AIDL contract.

## What This Module Contains

- `AssetArticleRepository`, which loads articles from `articles.json`.
- JSON parsing models and parser helpers.
- Runtime helpers:
  - `ScenarioController`
  - `RequestLogStore`
  - `CallerValidator`
- Shared AIDL transport DTOs and `.aidl` files.
- Unit tests for parsing, filtering inputs, and caller validation.

## What This Module Owns

- Loading backend article data.
- Keeping backend scenario state.
- Recording request logs for the visible control app.
- Validating whether a caller may use the backend service.
- Maintaining the backend side of the AIDL contract.

## Important Rule

The AIDL contract files in this module must stay compatible with the reader app's contract files. If one side changes the contract, update the other side in the same change.

## Useful Commands

```bash
./gradlew :features:server:data:testDebugUnitTest
```
