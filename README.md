# Unity News Two-App Architecture

This repository contains two independent Android projects for the news reader
architecture exercise:

- [`UnityNewsApp`](./UnityNewsApp): the reader/UI app.
- [`backend`](./backend): the local backend/control app.

The reader app communicates with the backend app through Android AIDL. The
backend owns the bundled JSON dataset and filtering logic. The reader owns the
user experience, dynamic filter controls, image rendering, and offline-first
cache.

## Reviewer Starting Points

- [Detailed architecture design](./DESIGN.md)
- [Code flow walkthrough](./CODE_FLOW_WALKTHROUGH.md)
- [Reader app README](./UnityNewsApp/README.md)
- [Backend app README](./backend/README.md)
- [Reader architecture notes](./UnityNewsApp/ARCHITECTURE.md)
- [Backend architecture notes](./backend/ARCHITECTURE.md)
- [Reader AI tooling write-up](./UnityNewsApp/AI_TOOLING.md)
- [Backend AI tooling write-up](./backend/AI_TOOLING.md)

## Build

Build and test the reader app:

```bash
cd UnityNewsApp
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

Build and test the backend app:

```bash
cd backend
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

## Demo Flow

1. Install the backend APK.
2. Open the backend app.
3. Start the backend foreground service from the console.
4. Install and open the reader app.
5. Apply title/rating filters in the reader.
6. Confirm the backend console request log updates.

## Repository Shape

Only these two Android projects are part of the submission:

```text
UnityNewsApp/
backend/
```

Generated files, screenshots, local Codex configuration, and accidental sample
projects are ignored through `.gitignore`.
