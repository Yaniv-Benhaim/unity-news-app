# Unity News

Unity News contains two independently installable Android applications:

- `UnityNewsApp`: a focused offline-first news reader.
- `backend`: an Android-native backend app that owns bundled articles, filtering, IPC, runtime status, and fault simulation.

## Architecture

The apps communicate over a versioned AIDL bound-service contract. The UI app renders cached data from Room and requests filtered article sets from the backend. The backend owns the bundled dataset at `backend/app/src/main/assets/articles.json`, derives filter specs from that data, and validates callers before serving requests.

The UI data layer depends on a `RemoteArticleDataSource` seam. Today that implementation is AIDL; a future server migration would add an HTTP implementation without changing domain or presentation.

## Build

```bash
(cd UnityNewsApp && ./gradlew :app:assembleDebug)
(cd backend && ./gradlew :app:assembleDebug)
```

## Test

```bash
(cd UnityNewsApp && ./gradlew testDebugUnitTest)
(cd backend && ./gradlew testDebugUnitTest)
./scripts/verify-aidl-contracts.sh
```

## Release Verification

```bash
(cd UnityNewsApp && ./gradlew :app:assembleRelease)
(cd backend && ./gradlew :app:assembleRelease)
```

Release builds enable R8 minification and resource shrinking. The ProGuard rules keep only the Parcelable IPC DTO surface required by Binder.

## Demo

1. Install the backend APK.
2. Install the UI APK signed with the same debug/release signing identity.
3. Open the backend app and start the foreground service.
4. Open the UI app.
5. Apply title, rating, and dynamic filters.
6. Change backend scenarios and observe loading, empty, stale, and error states in the UI.

## Security Notes

- The backend service is exported intentionally, but guarded by a signature permission.
- The backend validates caller UID, package name, and signing certificate.
- The UI app declares Android package visibility for the backend package and service action.
- The backend data is bundled locally; there is no runtime Gist or network fetch.
- Obfuscation raises the reverse-engineering cost, but Android permissions and caller validation are the real access-control mechanisms.

## AI Tooling Disclosure

AI tooling was used as an engineering assistant for build diagnosis, architecture comparison, planning, implementation support, and review loops. Human review drove the final architecture decisions.

Useful acceleration included identifying the AndroidX Core compile SDK mismatch and comparing inter-app communication options. Human correction rejected runtime Gist fetching and a local HTTP server after validating that the backend dataset must be bundled locally and that Android-native IPC better fits the security model.
