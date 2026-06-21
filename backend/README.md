# Unity News Backend

`backend` is an independently installable Android app that behaves like the local backend for the news reader.

## Responsibilities

- Bundle `app/src/main/assets/articles.json`.
- Derive filter specs from the bundled data.
- Execute backend-owned filtering.
- Expose a versioned AIDL bound service.
- Validate callers with a signature permission and runtime caller checks.
- Provide a visible operator console and foreground service controls.
- Simulate normal, slow, empty, server-error, and unauthorized scenarios.

## Build And Test

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
./gradlew :app:assembleRelease
```

## Demo Flow

1. Install the backend APK.
2. Open the backend app.
3. Start the foreground service.
4. Select a scenario from the console.
5. Open the UI app and apply filters.
6. Watch request logs update in the backend console.

## Security

The AIDL service is exported so the UI app can bind to it, but access is restricted with `com.unitynews.backend.permission.ACCESS_NEWS_BACKEND` and caller validation. The UI and backend must be signed with the same signing identity for the signature permission path.

## Company AI Skills

The `company-skills/` folder demonstrates internal Codex-style workflows for backend service changes, filter contract changes, and Android security review.

In a production organization these would be published as shared company skills so every developer follows the same architecture and review checklist.
