# Unity News Two-App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build two independently installable Android applications: a focused offline-first news reader and a secure Android-native backend app that owns bundled data, filtering, IPC, runtime controls, and fault simulation.

**Architecture:** The UI app talks to the backend app through a versioned AIDL bound-service contract. The UI app keeps Room as its source of truth and hides backend access behind `RemoteArticleDataSource`, so AIDL can later be replaced by HTTP without changing domain or presentation. The backend app owns bundled `articles.json`, filter specs, filter execution, caller validation, foreground-service runtime, and a visible operator console.

**Tech Stack:** Android Gradle Plugin 9.2.1 with built-in Kotlin, Jetpack Compose, Hilt 2.59.2, KSP 2.3.6, Room 2.8.4, Coil 2.7.0, kotlinx.serialization 1.11.0, kotlinx.coroutines-test 1.11.0, Turbine 1.2.1, JUnit 4.

---

## Implementation Notes

- Work one task at a time and commit after each completed task.
- Keep `UnityNewsApp` and `backend` as separate Gradle projects.
- Do not add a shared runtime module between the two projects.
- Put duplicated AIDL contracts under feature data modules, not app modules, so data modules can own IPC adapters without depending on `app`.
- Use AGP 9 built-in Kotlin in Android modules. Do not add `android.builtInKotlin=false`, `android.newDsl=false`, or `android.disallowKotlinSourceSets=false`.
- Keep UI filtering as criteria building only. Backend filtering remains authoritative.
- Whenever a test references Android framework classes, prefer Robolectric-free JVM tests first; use fakes and pure Kotlin where possible.

## File Map

### Repo-Level

- Create: `scripts/verify-aidl-contracts.sh` - compares duplicated AIDL contracts and Kotlin DTO contract files across projects.
- Modify: `docs/DESIGN.md` - update AIDL path from app modules to feature data modules.
- Create: `README.md` - review flow, build/test commands, AI tooling disclosure.

### UI App

- Modify: `UnityNewsApp/settings.gradle.kts` - include `core` and `features` modules.
- Modify: `UnityNewsApp/gradle/libs.versions.toml` - add Hilt, KSP, Room, Coil, serialization, coroutines-test, Turbine.
- Modify: `UnityNewsApp/app/build.gradle.kts` - app composition, Hilt, release hardening, module dependencies.
- Create: `UnityNewsApp/core/ui/build.gradle.kts`
- Create: `UnityNewsApp/core/data/build.gradle.kts`
- Create: `UnityNewsApp/features/news/domain/build.gradle.kts`
- Create: `UnityNewsApp/features/news/data/build.gradle.kts`
- Create: `UnityNewsApp/features/news/presentation/build.gradle.kts`
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/...`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/...`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/...`
- Create: `UnityNewsApp/features/news/presentation/src/main/java/com/unitynews/news/presentation/...`

### Backend App

- Modify: `backend/settings.gradle.kts` - include `core` and `features` modules.
- Modify: `backend/gradle/libs.versions.toml` - mirror dependency catalog.
- Modify: `backend/app/build.gradle.kts` - Hilt, release hardening, module dependencies.
- Create: `backend/core/ui/build.gradle.kts`
- Create: `backend/core/data/build.gradle.kts`
- Create: `backend/features/server/domain/build.gradle.kts`
- Create: `backend/features/server/data/build.gradle.kts`
- Create: `backend/features/server/presentation/build.gradle.kts`
- Create: `backend/app/src/main/assets/articles.json`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/...`
- Create: `backend/features/server/data/src/main/aidl/com/unitynews/contract/...`
- Create: `backend/features/server/data/src/main/java/com/unitynews/server/data/...`
- Create: `backend/features/server/presentation/src/main/java/com/unitynews/server/presentation/...`
- Modify: `backend/app/src/main/AndroidManifest.xml` - permission, service, foreground-service declarations.

---

## Task 1: Gradle Modules And Dependency Baseline

**Files:**
- Modify: `UnityNewsApp/settings.gradle.kts`
- Modify: `UnityNewsApp/gradle/libs.versions.toml`
- Modify: `UnityNewsApp/app/build.gradle.kts`
- Create: `UnityNewsApp/core/ui/build.gradle.kts`
- Create: `UnityNewsApp/core/data/build.gradle.kts`
- Create: `UnityNewsApp/features/news/domain/build.gradle.kts`
- Create: `UnityNewsApp/features/news/data/build.gradle.kts`
- Create: `UnityNewsApp/features/news/presentation/build.gradle.kts`
- Modify: `backend/settings.gradle.kts`
- Modify: `backend/gradle/libs.versions.toml`
- Modify: `backend/app/build.gradle.kts`
- Create: `backend/core/ui/build.gradle.kts`
- Create: `backend/core/data/build.gradle.kts`
- Create: `backend/features/server/domain/build.gradle.kts`
- Create: `backend/features/server/data/build.gradle.kts`
- Create: `backend/features/server/presentation/build.gradle.kts`

- [ ] **Step 1: Add module includes to both settings files**

Use this module block in `UnityNewsApp/settings.gradle.kts` after `include(":app")`:

```kotlin
include(":core:ui")
include(":core:data")
include(":features:news:domain")
include(":features:news:data")
include(":features:news:presentation")
```

Use this module block in `backend/settings.gradle.kts` after `include(":app")`:

```kotlin
include(":core:ui")
include(":core:data")
include(":features:server:domain")
include(":features:server:data")
include(":features:server:presentation")
```

- [ ] **Step 2: Add shared dependency catalog entries to both projects**

Add these versions, libraries, and plugins to both `gradle/libs.versions.toml` files:

```toml
[versions]
hilt = "2.59.2"
ksp = "2.3.6"
room = "2.8.4"
coil = "2.7.0"
serializationJson = "1.11.0"
coroutines = "1.11.0"
turbine = "1.2.1"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serializationJson" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

[plugins]
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 3: Create Android library module build files**

For Compose UI modules (`core/ui`, `features/news/presentation`, `features/server/presentation`), use this shape and update `namespace` per module:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.unitynews.core.ui"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.coil.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

For pure domain modules, use:

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    testImplementation(libs.junit)
}
```

For data modules, use this shape and update `namespace` plus dependencies:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.unitynews.news.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
```

- [ ] **Step 4: Wire app dependencies**

In `UnityNewsApp/app/build.gradle.kts`, add Hilt plugin and module dependencies:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":features:news:domain"))
    implementation(project(":features:news:data"))
    implementation(project(":features:news:presentation"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

In `backend/app/build.gradle.kts`, use equivalent dependencies:

```kotlin
dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":features:server:domain"))
    implementation(project(":features:server:data"))
    implementation(project(":features:server:presentation"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
```

- [ ] **Step 5: Verify both projects still build**

Run:

```bash
(cd UnityNewsApp && ./gradlew :app:assembleDebug)
(cd backend && ./gradlew :app:assembleDebug)
```

Expected: both commands finish with `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add UnityNewsApp backend
git commit -m "chore: add modular project structure"
```

---

## Task 2: Versioned AIDL Contract And Drift Verification

**Files:**
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/INewsBackendService.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/IArticlesCallback.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/IFilterSpecsCallback.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/IBackendStatusCallback.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/ArticleDto.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/ArticleFilterRequest.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/FilterSpecDto.aidl`
- Create: `UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/BackendStatusDto.aidl`
- Mirror the same files under: `backend/features/server/data/src/main/aidl/com/unitynews/contract/`
- Create Kotlin Parcelable implementations in both data modules under `src/main/java/com/unitynews/contract/`
- Create: `scripts/verify-aidl-contracts.sh`
- Modify: `docs/DESIGN.md`

- [ ] **Step 1: Write duplicated AIDL interfaces**

Use this exact `INewsBackendService.aidl` in both projects:

```aidl
package com.unitynews.contract;

import com.unitynews.contract.ArticleFilterRequest;
import com.unitynews.contract.IArticlesCallback;
import com.unitynews.contract.IFilterSpecsCallback;
import com.unitynews.contract.IBackendStatusCallback;

interface INewsBackendService {
    int getApiVersion();
    void getFilterSpecs(in IFilterSpecsCallback callback);
    void getArticles(in ArticleFilterRequest request, in IArticlesCallback callback);
    void getBackendStatus(in IBackendStatusCallback callback);
}
```

Use this `IArticlesCallback.aidl`:

```aidl
package com.unitynews.contract;

import com.unitynews.contract.ArticleDto;

interface IArticlesCallback {
    void onSuccess(in List<ArticleDto> articles);
    void onError(String code, String message);
}
```

Use this `IFilterSpecsCallback.aidl`:

```aidl
package com.unitynews.contract;

import com.unitynews.contract.FilterSpecDto;

interface IFilterSpecsCallback {
    void onSuccess(in List<FilterSpecDto> specs);
    void onError(String code, String message);
}
```

Use this `IBackendStatusCallback.aidl`:

```aidl
package com.unitynews.contract;

import com.unitynews.contract.BackendStatusDto;

interface IBackendStatusCallback {
    void onSuccess(in BackendStatusDto status);
    void onError(String code, String message);
}
```

Use parcelable declarations:

```aidl
package com.unitynews.contract;
parcelable ArticleDto;
```

```aidl
package com.unitynews.contract;
parcelable ArticleFilterRequest;
```

```aidl
package com.unitynews.contract;
parcelable FilterSpecDto;
```

```aidl
package com.unitynews.contract;
parcelable BackendStatusDto;
```

- [ ] **Step 2: Write Kotlin Parcelable DTOs in both data modules**

Create `ArticleDto.kt` as an explicit `Parcelable` implementation:

```kotlin
package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class ArticleDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString().orEmpty(),
        title = parcel.readString().orEmpty(),
        description = parcel.readString().orEmpty(),
        imageUrl = parcel.readString().orEmpty(),
        rating = parcel.readInt(),
        placeholderRed = parcel.readInt(),
        placeholderGreen = parcel.readInt(),
        placeholderBlue = parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeInt(rating)
        parcel.writeInt(placeholderRed)
        parcel.writeInt(placeholderGreen)
        parcel.writeInt(placeholderBlue)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArticleDto> {
        override fun createFromParcel(parcel: Parcel): ArticleDto = ArticleDto(parcel)

        override fun newArray(size: Int): Array<ArticleDto?> = arrayOfNulls(size)
    }
}
```

Create `ArticleFilterRequest.kt` as an explicit `Parcelable` implementation:

```kotlin
package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class ArticleFilterRequest(
    val titleQuery: String?,
    val ratingValues: List<Int>,
    val requestId: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        titleQuery = parcel.readString(),
        ratingValues = parcel.createIntArray()?.toList().orEmpty(),
        requestId = parcel.readString().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(titleQuery)
        parcel.writeIntArray(ratingValues.toIntArray())
        parcel.writeString(requestId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArticleFilterRequest> {
        override fun createFromParcel(parcel: Parcel): ArticleFilterRequest =
            ArticleFilterRequest(parcel)

        override fun newArray(size: Int): Array<ArticleFilterRequest?> = arrayOfNulls(size)
    }
}
```

Create `FilterSpecDto.kt` as an explicit `Parcelable` implementation:

```kotlin
package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class FilterSpecDto(
    val key: String,
    val label: String,
    val type: String,
    val options: List<String>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        key = parcel.readString().orEmpty(),
        label = parcel.readString().orEmpty(),
        type = parcel.readString().orEmpty(),
        options = parcel.createStringArrayList().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(label)
        parcel.writeString(type)
        parcel.writeStringList(options)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FilterSpecDto> {
        override fun createFromParcel(parcel: Parcel): FilterSpecDto = FilterSpecDto(parcel)

        override fun newArray(size: Int): Array<FilterSpecDto?> = arrayOfNulls(size)
    }
}
```

Create `BackendStatusDto.kt` as an explicit `Parcelable` implementation:

```kotlin
package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class BackendStatusDto(
    val isRunning: Boolean,
    val scenario: String,
    val articleCount: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        isRunning = parcel.readInt() != 0,
        scenario = parcel.readString().orEmpty(),
        articleCount = parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (isRunning) 1 else 0)
        parcel.writeString(scenario)
        parcel.writeInt(articleCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BackendStatusDto> {
        override fun createFromParcel(parcel: Parcel): BackendStatusDto = BackendStatusDto(parcel)

        override fun newArray(size: Int): Array<BackendStatusDto?> = arrayOfNulls(size)
    }
}
```

Do not add a custom Parcelize compiler configuration or legacy AGP Kotlin opt-out flags.

- [ ] **Step 3: Add contract drift script**

Create `scripts/verify-aidl-contracts.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

UI_AIDL_DIR="UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract"
BACKEND_AIDL_DIR="backend/features/server/data/src/main/aidl/com/unitynews/contract"
UI_DTO_DIR="UnityNewsApp/features/news/data/src/main/java/com/unitynews/contract"
BACKEND_DTO_DIR="backend/features/server/data/src/main/java/com/unitynews/contract"

if [[ ! -d "$UI_AIDL_DIR" ]]; then
  echo "Missing UI AIDL contract directory: $UI_AIDL_DIR" >&2
  exit 1
fi

if [[ ! -d "$BACKEND_AIDL_DIR" ]]; then
  echo "Missing backend AIDL contract directory: $BACKEND_AIDL_DIR" >&2
  exit 1
fi

if [[ ! -d "$UI_DTO_DIR" ]]; then
  echo "Missing UI DTO contract directory: $UI_DTO_DIR" >&2
  exit 1
fi

if [[ ! -d "$BACKEND_DTO_DIR" ]]; then
  echo "Missing backend DTO contract directory: $BACKEND_DTO_DIR" >&2
  exit 1
fi

diff -ru "$UI_AIDL_DIR" "$BACKEND_AIDL_DIR"
diff -ru "$UI_DTO_DIR" "$BACKEND_DTO_DIR"
echo "AIDL and DTO contracts match."
```

Run:

```bash
chmod +x scripts/verify-aidl-contracts.sh
./scripts/verify-aidl-contracts.sh
```

Expected: `AIDL and DTO contracts match.`

- [ ] **Step 4: Confirm design doc AIDL paths**

In `docs/DESIGN.md`, ensure the documented AIDL paths are:

```text
UnityNewsApp/features/news/data/src/main/aidl/com/unitynews/contract/
backend/features/server/data/src/main/aidl/com/unitynews/contract/
```

- [ ] **Step 5: Verify generated AIDL compiles**

Run:

```bash
(cd UnityNewsApp && ./gradlew :features:news:data:assembleDebug)
(cd backend && ./gradlew :features:server:data:assembleDebug)
./scripts/verify-aidl-contracts.sh
```

Expected: all three commands pass.

- [ ] **Step 6: Commit**

```bash
git add UnityNewsApp backend scripts docs/DESIGN.md
git commit -m "feat: add versioned aidl contract"
```

---

## Task 3: Backend Domain, JSON Asset, Filtering, And Tests

**Files:**
- Create: `backend/app/src/main/assets/articles.json`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/model/Article.kt`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/model/FilterCriteria.kt`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/model/FilterSpec.kt`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/model/ServerScenario.kt`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/repository/ArticleRepository.kt`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/usecase/FilterArticlesUseCase.kt`
- Create: `backend/features/server/domain/src/main/java/com/unitynews/server/domain/usecase/GetFilterSpecsUseCase.kt`
- Create tests under: `backend/features/server/domain/src/test/java/com/unitynews/server/domain/usecase/`

- [ ] **Step 1: Copy the bundled dataset**

Download once and save as local asset:

```bash
mkdir -p backend/app/src/main/assets
curl -Ls "https://gist.githubusercontent.com/ironLeeC/fe70a36a28358cf356fe4168398c8e6e/raw/ca137334c316b472659d1f90521218ef83735875/articles.json" \
  -o backend/app/src/main/assets/articles.json
```

Run:

```bash
jq '.articles | length' backend/app/src/main/assets/articles.json
```

Expected: `129`.

- [ ] **Step 2: Write failing backend filter tests**

Create `FilterArticlesUseCaseTest.kt`:

```kotlin
package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterCriteria
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterArticlesUseCaseTest {
    private val articles = listOf(
        Article("1", "Unity releases new tools", "Game dev news", "url1", 5, 1, 2, 3),
        Article("2", "Android architecture guide", "Mobile news", "url2", 3, 4, 5, 6),
        Article("3", "Unity earnings report", "Business news", "url3", 1, 7, 8, 9),
    )

    private val useCase = FilterArticlesUseCase()

    @Test
    fun `empty criteria returns all articles`() {
        assertEquals(articles, useCase(articles, FilterCriteria()))
    }

    @Test
    fun `title filter is case insensitive contains`() {
        val result = useCase(articles, FilterCriteria(titleQuery = "UNITY"))
        assertEquals(listOf(articles[0], articles[2]), result)
    }

    @Test
    fun `rating filter supports multiple values`() {
        val result = useCase(articles, FilterCriteria(ratingValues = setOf(1, 5)))
        assertEquals(listOf(articles[0], articles[2]), result)
    }

    @Test
    fun `title and rating filters are applied together`() {
        val result = useCase(articles, FilterCriteria(titleQuery = "unity", ratingValues = setOf(5)))
        assertEquals(listOf(articles[0]), result)
    }
}
```

Run:

```bash
(cd backend && ./gradlew :features:server:domain:test)
```

Expected: fails because domain types do not exist.

- [ ] **Step 3: Implement backend domain models and filter use case**

Create `Article.kt`:

```kotlin
package com.unitynews.server.domain.model

data class Article(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
)
```

Create `FilterCriteria.kt`:

```kotlin
package com.unitynews.server.domain.model

data class FilterCriteria(
    val titleQuery: String? = null,
    val ratingValues: Set<Int> = emptySet(),
)
```

Create `FilterArticlesUseCase.kt`:

```kotlin
package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterCriteria

class FilterArticlesUseCase {
    operator fun invoke(articles: List<Article>, criteria: FilterCriteria): List<Article> {
        val normalizedTitle = criteria.titleQuery?.trim().orEmpty()
        return articles.filter { article ->
            val matchesTitle = normalizedTitle.isBlank() ||
                article.title.contains(normalizedTitle, ignoreCase = true)
            val matchesRating = criteria.ratingValues.isEmpty() ||
                article.rating in criteria.ratingValues
            matchesTitle && matchesRating
        }
    }
}
```

- [ ] **Step 4: Add filter spec use case and tests**

Create `GetFilterSpecsUseCaseTest.kt`:

```kotlin
package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFilterSpecsUseCaseTest {
    @Test
    fun `rating options are derived from dataset`() {
        val articles = listOf(
            Article("1", "A", "D", "url", 5, 0, 0, 0),
            Article("2", "B", "D", "url", 1, 0, 0, 0),
            Article("3", "C", "D", "url", 5, 0, 0, 0),
        )

        val specs = GetFilterSpecsUseCase()(articles)
        assertEquals("title", specs[0].key)
        assertEquals(listOf("1", "5"), specs[1].options)
    }
}
```

Create `FilterSpec.kt`:

```kotlin
package com.unitynews.server.domain.model

data class FilterSpec(
    val key: String,
    val label: String,
    val type: FilterType,
    val options: List<String> = emptyList(),
)

enum class FilterType {
    Text,
    MultiSelect,
}
```

Create `GetFilterSpecsUseCase.kt`:

```kotlin
package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterSpec
import com.unitynews.server.domain.model.FilterType

class GetFilterSpecsUseCase {
    operator fun invoke(articles: List<Article>): List<FilterSpec> {
        val ratings = articles.map { it.rating }.distinct().sorted().map { it.toString() }
        return listOf(
            FilterSpec(key = "title", label = "Title", type = FilterType.Text),
            FilterSpec(key = "rating", label = "Rating", type = FilterType.MultiSelect, options = ratings),
        )
    }
}
```

- [ ] **Step 5: Verify backend domain tests**

Run:

```bash
(cd backend && ./gradlew :features:server:domain:test)
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add backend
git commit -m "feat: add backend filtering domain"
```

---

## Task 4: Backend Data, AIDL Service Adapter, Security, Scenarios, And Console

**Files:**
- Create: `backend/features/server/data/src/main/java/com/unitynews/server/data/AssetArticleRepository.kt`
- Create: `backend/features/server/data/src/main/java/com/unitynews/server/data/ArticleJsonModels.kt`
- Create: `backend/features/server/data/src/main/java/com/unitynews/server/data/RequestLogStore.kt`
- Create: `backend/features/server/data/src/main/java/com/unitynews/server/data/CallerValidator.kt`
- Create: `backend/features/server/data/src/main/java/com/unitynews/server/data/ScenarioController.kt`
- Create: `backend/app/src/main/java/com/example/unitynewsbackend/UnityNewsBackendApplication.kt`
- Create: `backend/app/src/main/java/com/example/unitynewsbackend/service/NewsBackendService.kt`
- Create: `backend/app/src/main/java/com/example/unitynewsbackend/service/NewsBackendForegroundService.kt`
- Modify: `backend/app/src/main/AndroidManifest.xml`
- Create: `backend/features/server/presentation/src/main/java/com/unitynews/server/presentation/BackendConsoleViewModel.kt`
- Create: `backend/features/server/presentation/src/main/java/com/unitynews/server/presentation/BackendConsoleScreen.kt`
- Modify: `backend/app/src/main/java/com/example/unitynewsbackend/MainActivity.kt`

- [ ] **Step 1: Write JSON parser test**

Create `AssetArticleRepositoryTest.kt` in backend data tests:

```kotlin
package com.unitynews.server.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AssetArticleRepositoryTest {
    @Test
    fun `parser maps article json into domain articles`() {
        val json = """
            {"articles":[{"title":"A","description":"D","image_url":"url","rating":4,"placeholderColor":{"red":1,"green":2,"blue":3}}]}
        """.trimIndent()

        val articles = ArticleJsonParser().parse(json)

        assertEquals(1, articles.size)
        assertEquals("A", articles.single().title)
        assertEquals(4, articles.single().rating)
        assertEquals(1, articles.single().placeholderRed)
    }
}
```

- [ ] **Step 2: Implement JSON parser**

Create `ArticleJsonModels.kt`:

```kotlin
package com.unitynews.server.data

import com.unitynews.server.domain.model.Article
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest

@Serializable
private data class ArticlesPayload(val articles: List<ArticlePayload>)

@Serializable
private data class ArticlePayload(
    val title: String,
    val description: String,
    @SerialName("image_url") val imageUrl: String,
    val rating: Int,
    val placeholderColor: PlaceholderColorPayload,
)

@Serializable
private data class PlaceholderColorPayload(val red: Int, val green: Int, val blue: Int)

class ArticleJsonParser(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun parse(rawJson: String): List<Article> {
        return json.decodeFromString<ArticlesPayload>(rawJson).articles.map { payload ->
            Article(
                id = stableId(payload.title, payload.imageUrl),
                title = payload.title,
                description = payload.description,
                imageUrl = payload.imageUrl,
                rating = payload.rating,
                placeholderRed = payload.placeholderColor.red,
                placeholderGreen = payload.placeholderColor.green,
                placeholderBlue = payload.placeholderColor.blue,
            )
        }
    }

    private fun stableId(title: String, imageUrl: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$title|$imageUrl".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
```

- [ ] **Step 3: Implement backend service manifest**

Update `backend/app/src/main/AndroidManifest.xml` with:

```xml
<permission
    android:name="com.unitynews.backend.permission.ACCESS_NEWS_BACKEND"
    android:protectionLevel="signature" />

<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application
    android:name=".UnityNewsBackendApplication"
    ...>

    <service
        android:name=".service.NewsBackendService"
        android:exported="true"
        android:permission="com.unitynews.backend.permission.ACCESS_NEWS_BACKEND" />

    <service
        android:name=".service.NewsBackendForegroundService"
        android:exported="false"
        android:foregroundServiceType="dataSync" />
</application>
```

Preserve existing theme/activity entries while adding these declarations.

- [ ] **Step 4: Implement request scenarios**

Create `ServerScenario.kt` in backend domain if not already present:

```kotlin
package com.unitynews.server.domain.model

enum class ServerScenario {
    Normal,
    Slow,
    Empty,
    ServerError,
    Unauthorized,
}
```

Create `ScenarioController.kt`:

```kotlin
package com.unitynews.server.data

import com.unitynews.server.domain.model.ServerScenario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ScenarioController {
    private val _scenario = MutableStateFlow(ServerScenario.Normal)
    val scenario: StateFlow<ServerScenario> = _scenario

    fun setScenario(value: ServerScenario) {
        _scenario.value = value
    }
}
```

- [ ] **Step 5: Implement AIDL service stub**

Create `NewsBackendService.kt`:

```kotlin
package com.example.unitynewsbackend.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.BackendStatusDto
import com.unitynews.contract.IArticlesCallback
import com.unitynews.contract.IBackendStatusCallback
import com.unitynews.contract.IFilterSpecsCallback
import com.unitynews.contract.INewsBackendService

class NewsBackendService : Service() {
    override fun onBind(intent: Intent?): IBinder = binder

    private val binder = object : INewsBackendService.Stub() {
        override fun getApiVersion(): Int = 1

        override fun getFilterSpecs(callback: IFilterSpecsCallback) {
            callback.onSuccess(mutableListOf())
        }

        override fun getArticles(request: ArticleFilterRequest, callback: IArticlesCallback) {
            callback.onSuccess(mutableListOf())
        }

        override fun getBackendStatus(callback: IBackendStatusCallback) {
            callback.onSuccess(BackendStatusDto(isRunning = true, scenario = "Normal", articleCount = 0))
        }
    }
}
```

This compiles the service boundary first with deterministic empty responses. Subsequent commits replace the empty response implementation with injected use cases while preserving the same AIDL surface.

- [ ] **Step 6: Build backend service and console UI incrementally**

Create `BackendConsoleUiState.kt`:

```kotlin
package com.unitynews.server.presentation

data class BackendConsoleUiState(
    val isServiceRunning: Boolean = false,
    val scenario: String = "Normal",
    val articleCount: Int = 0,
    val requestLogs: List<String> = emptyList(),
)
```

Create `BackendConsoleScreen.kt`:

```kotlin
package com.unitynews.server.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BackendConsoleScreen(
    state: BackendConsoleUiState,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onScenarioSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Unity News Backend")
        Text("Service: ${if (state.isServiceRunning) "Running" else "Stopped"}")
        Text("Scenario: ${state.scenario}")
        Text("Articles: ${state.articleCount}")
        Button(onClick = onStartService) { Text("Start Service") }
        Button(onClick = onStopService) { Text("Stop Service") }
        listOf("Normal", "Slow", "Empty", "ServerError", "Unauthorized").forEach { scenario ->
            Button(onClick = { onScenarioSelected(scenario) }) { Text(scenario) }
        }
        state.requestLogs.forEach { Text(it) }
    }
}
```

- [ ] **Step 7: Verify backend app**

Run:

```bash
(cd backend && ./gradlew :features:server:data:testDebugUnitTest)
(cd backend && ./gradlew :app:assembleDebug)
```

Expected: both commands pass. If `testDebugUnitTest` is unavailable for a library module, run `(cd backend && ./gradlew :features:server:data:test)`.

- [ ] **Step 8: Commit**

```bash
git add backend
git commit -m "feat: add backend data service and console"
```

---

## Task 5: UI Domain, Room Cache, Repository, And Tests

**Files:**
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/model/Article.kt`
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/model/FilterCriteria.kt`
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/model/FilterSpec.kt`
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/repository/NewsRepository.kt`
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/usecase/ObserveArticlesUseCase.kt`
- Create: `UnityNewsApp/features/news/domain/src/main/java/com/unitynews/news/domain/usecase/RefreshArticlesUseCase.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/local/ArticleEntity.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/local/CachedQueryEntity.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/local/NewsDao.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/local/NewsDatabase.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/OfflineFirstNewsRepository.kt`
- Create tests under: `UnityNewsApp/features/news/data/src/test/java/com/unitynews/news/data/`

- [ ] **Step 0: Add Room dependencies to the UI news data module**

In `UnityNewsApp/features/news/data/build.gradle.kts`, add:

```kotlin
dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
```

- [ ] **Step 1: Write repository cache behavior test**

Create `OfflineFirstNewsRepositoryTest.kt`:

```kotlin
package com.unitynews.news.data

import app.cash.turbine.test
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineFirstNewsRepositoryTest {
    @Test
    fun `refresh success stores articles by criteria and observer emits them`() = runTest {
        val remote = FakeRemoteArticleDataSource(
            result = Result.success(listOf(Article("1", "Unity", "D", "url", 5, 1, 2, 3)))
        )
        val local = InMemoryNewsLocalDataSource()
        val repository = OfflineFirstNewsRepository(local, remote)
        val criteria = FilterCriteria(titleQuery = "unity", ratingValues = setOf(5))

        repository.refresh(criteria)

        repository.observeArticles(criteria).test {
            assertEquals("Unity", awaitItem().single().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh failure preserves cached articles`() = runTest {
        val local = InMemoryNewsLocalDataSource()
        val criteria = FilterCriteria()
        local.replace(criteria, listOf(Article("1", "Cached", "D", "url", 3, 1, 2, 3)))
        val remote = FakeRemoteArticleDataSource(result = Result.failure(IllegalStateException("offline")))
        val repository = OfflineFirstNewsRepository(local, remote)

        repository.refresh(criteria)

        repository.observeArticles(criteria).test {
            assertEquals("Cached", awaitItem().single().title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Implement UI domain models**

Create `Article.kt`:

```kotlin
package com.unitynews.news.domain.model

data class Article(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
)
```

Create `FilterCriteria.kt`:

```kotlin
package com.unitynews.news.domain.model

data class FilterCriteria(
    val titleQuery: String? = null,
    val ratingValues: Set<Int> = emptySet(),
)
```

Create `FilterSpec.kt`:

```kotlin
package com.unitynews.news.domain.model

data class FilterSpec(
    val key: String,
    val label: String,
    val type: FilterType,
    val options: List<String> = emptyList(),
)

enum class FilterType {
    Text,
    MultiSelect,
    Unsupported,
}
```

Create `NewsRepository.kt`:

```kotlin
package com.unitynews.news.domain.repository

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun observeArticles(criteria: FilterCriteria): Flow<List<Article>>
    suspend fun refresh(criteria: FilterCriteria): Result<Unit>
    suspend fun getFilterSpecs(): Result<List<FilterSpec>>
}
```

- [ ] **Step 3: Implement repository seams**

Create `RemoteArticleDataSource.kt`:

```kotlin
package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec

interface RemoteArticleDataSource {
    suspend fun getArticles(criteria: FilterCriteria): Result<List<Article>>
    suspend fun getFilterSpecs(): Result<List<FilterSpec>>
}
```

Create `NewsLocalDataSource.kt`:

```kotlin
package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.flow.Flow

interface NewsLocalDataSource {
    fun observe(criteria: FilterCriteria): Flow<List<Article>>
    suspend fun replace(criteria: FilterCriteria, articles: List<Article>)
    suspend fun markStale(criteria: FilterCriteria, reason: String)
}
```

Create `OfflineFirstNewsRepository.kt`:

```kotlin
package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class OfflineFirstNewsRepository(
    private val local: NewsLocalDataSource,
    private val remote: RemoteArticleDataSource,
) : NewsRepository {
    override fun observeArticles(criteria: FilterCriteria): Flow<List<Article>> = local.observe(criteria)

    override suspend fun refresh(criteria: FilterCriteria): Result<Unit> {
        return remote.getArticles(criteria).fold(
            onSuccess = { articles ->
                local.replace(criteria, articles)
                Result.success(Unit)
            },
            onFailure = { error ->
                local.markStale(criteria, error.message ?: "Remote refresh failed")
                Result.failure(error)
            },
        )
    }

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> = remote.getFilterSpecs()
}
```

- [ ] **Step 4: Add Room entities and DAO**

Create `ArticleEntity.kt`:

```kotlin
package com.unitynews.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
    val lastFetchedAt: Long,
)
```

Create `CachedQueryEntity.kt`:

```kotlin
package com.unitynews.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_queries")
data class CachedQueryEntity(
    @PrimaryKey val criteriaHash: String,
    val articleIds: String,
    val lastSuccessfulRefreshAt: Long,
    val staleReason: String?,
)
```

Create `NewsDatabase.kt`:

```kotlin
package com.unitynews.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ArticleEntity::class, CachedQueryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}
```

- [ ] **Step 5: Verify UI data tests**

Run:

```bash
(cd UnityNewsApp && ./gradlew :features:news:data:testDebugUnitTest)
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add UnityNewsApp
git commit -m "feat: add offline first news repository"
```

---

## Task 6: UI AIDL Data Source And Backend Setup Detection

**Files:**
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/aidl/AidlArticleDataSource.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/aidl/BackendConnection.kt`
- Create: `UnityNewsApp/features/news/data/src/main/java/com/unitynews/news/data/aidl/BackendAvailabilityChecker.kt`
- Create tests under: `UnityNewsApp/features/news/data/src/test/java/com/unitynews/news/data/aidl/`

- [ ] **Step 1: Write availability checker test**

Create `BackendAvailabilityCheckerTest.kt`:

```kotlin
package com.unitynews.news.data.aidl

import org.junit.Assert.assertEquals
import org.junit.Test

class BackendAvailabilityCheckerTest {
    @Test
    fun `missing package maps to backend missing`() {
        val checker = BackendAvailabilityChecker(FakePackageInspector(isInstalled = false))
        assertEquals(BackendAvailability.Missing, checker.check())
    }

    @Test
    fun `installed package maps to installed`() {
        val checker = BackendAvailabilityChecker(FakePackageInspector(isInstalled = true))
        assertEquals(BackendAvailability.Installed, checker.check())
    }
}
```

- [ ] **Step 2: Implement backend availability types**

Create `BackendAvailabilityChecker.kt`:

```kotlin
package com.unitynews.news.data.aidl

interface PackageInspector {
    fun isPackageInstalled(packageName: String): Boolean
}

enum class BackendAvailability {
    Missing,
    Installed,
}

class BackendAvailabilityChecker(
    private val packageInspector: PackageInspector,
    private val backendPackageName: String = "com.example.unitynewsbackend",
) {
    fun check(): BackendAvailability {
        return if (packageInspector.isPackageInstalled(backendPackageName)) {
            BackendAvailability.Installed
        } else {
            BackendAvailability.Missing
        }
    }
}
```

- [ ] **Step 3: Implement AIDL callback bridge**

Create `AidlArticleDataSource.kt`:

```kotlin
package com.unitynews.news.data.aidl

import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.IArticlesCallback
import com.unitynews.contract.INewsBackendService
import com.unitynews.news.data.RemoteArticleDataSource
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AidlArticleDataSource(
    private val backend: suspend () -> INewsBackendService,
) : RemoteArticleDataSource {
    override suspend fun getArticles(criteria: FilterCriteria): Result<List<Article>> {
        val service = backend()
        if (service.apiVersion != 2) return Result.failure(IllegalStateException("Unsupported backend version"))
        return suspendCancellableCoroutine { continuation ->
            service.getArticles(criteria.toRequest(), object : IArticlesCallback.Stub() {
                override fun onSuccess(articles: MutableList<com.unitynews.contract.ArticleDto>) {
                    continuation.resume(Result.success(articles.map { it.toDomain() }))
                }

                override fun onError(code: String, message: String) {
                    continuation.resume(Result.failure(IllegalStateException("$code: $message")))
                }
            })
        }
    }

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> {
        return Result.success(emptyList())
    }

    private fun FilterCriteria.toRequest(): ArticleFilterRequest {
        return ArticleFilterRequest(
            titleQuery = titleQuery,
            ratingValues = ratingValues.toList(),
            requestId = System.currentTimeMillis().toString(),
            dynamicValues = dynamicValues.mapValues { (_, values) -> values.toList() },
        )
    }

    private fun com.unitynews.contract.ArticleDto.toDomain(): Article {
        return Article(id, title, description, imageUrl, rating, placeholderRed, placeholderGreen, placeholderBlue)
    }
}
```

- [ ] **Step 4: Verify UI data module**

Run:

```bash
(cd UnityNewsApp && ./gradlew :features:news:data:testDebugUnitTest)
(cd UnityNewsApp && ./gradlew :features:news:data:assembleDebug)
```

Expected: both commands pass.

- [ ] **Step 5: Commit**

```bash
git add UnityNewsApp
git commit -m "feat: add aidl news data source"
```

---

## Task 7: UI Presentation, Dynamic Filters, Image Placeholders, And Onboarding

**Files:**
- Create: `UnityNewsApp/features/news/presentation/src/main/java/com/unitynews/news/presentation/NewsUiState.kt`
- Create: `UnityNewsApp/features/news/presentation/src/main/java/com/unitynews/news/presentation/NewsViewModel.kt`
- Create: `UnityNewsApp/features/news/presentation/src/main/java/com/unitynews/news/presentation/NewsScreen.kt`
- Create: `UnityNewsApp/features/news/presentation/src/main/java/com/unitynews/news/presentation/FilterControls.kt`
- Create: `UnityNewsApp/features/news/presentation/src/main/java/com/unitynews/news/presentation/BackendSetupScreen.kt`
- Modify: `UnityNewsApp/app/src/main/java/com/example/unitynewsapp/MainActivity.kt`

- [ ] **Step 1: Write ViewModel state test**

Create `NewsViewModelTest.kt`:

```kotlin
package com.unitynews.news.presentation

import app.cash.turbine.test
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NewsViewModelTest {
    @Test
    fun `content state contains cached articles`() = runTest {
        val repository = FakeNewsRepository(
            articles = MutableStateFlow(listOf(Article("1", "Unity", "D", "url", 5, 1, 2, 3)))
        )
        val viewModel = NewsViewModel(repository)

        viewModel.uiState.test {
            assertEquals("Unity", (awaitItem() as NewsUiState.Content).articles.single().title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

- [ ] **Step 2: Implement UI state**

Create `NewsUiState.kt`:

```kotlin
package com.unitynews.news.presentation

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterSpec

sealed interface NewsUiState {
    data object InitialLoading : NewsUiState
    data class Content(
        val articles: List<Article>,
        val filters: List<FilterSpec> = emptyList(),
        val isRefreshing: Boolean = false,
        val staleMessage: String? = null,
    ) : NewsUiState
    data object Empty : NewsUiState
    data object BackendMissing : NewsUiState
    data class Error(val message: String) : NewsUiState
}
```

- [ ] **Step 3: Implement focused news screen**

Create `NewsScreen.kt`:

```kotlin
package com.unitynews.news.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unitynews.news.domain.model.Article

@Composable
fun NewsScreen(
    state: NewsUiState,
    onApplyFilters: () -> Unit,
    onOpenBackendSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        NewsUiState.InitialLoading -> Text("Loading news...", modifier.padding(16.dp))
        NewsUiState.Empty -> Text("No articles match the selected filters.", modifier.padding(16.dp))
        NewsUiState.BackendMissing -> BackendSetupScreen(onOpenBackendSetup, modifier)
        is NewsUiState.Error -> Text(state.message, modifier.padding(16.dp))
        is NewsUiState.Content -> Column(modifier) {
            Button(onClick = onApplyFilters, modifier = Modifier.padding(16.dp)) { Text("Apply") }
            LazyColumn {
                items(state.articles, key = { it.id }) { article ->
                    ArticleRow(article)
                }
            }
        }
    }
}

@Composable
private fun ArticleRow(article: Article) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .background(Color(article.placeholderRed, article.placeholderGreen, article.placeholderBlue))
                    .weight(1f),
            )
            Column(modifier = Modifier.weight(2f).padding(start = 12.dp)) {
                Text(article.title)
                Text(article.description)
                Text("Rating ${article.rating}")
            }
        }
    }
}
```

- [ ] **Step 4: Implement setup screen**

Create `BackendSetupScreen.kt`:

```kotlin
package com.unitynews.news.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BackendSetupScreen(
    onOpenBackendSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Backend app required")
        Text("Install or open the companion backend app, start the backend service, then return here.")
        Button(onClick = onOpenBackendSetup) {
            Text("Open Backend Setup")
        }
    }
}
```

- [ ] **Step 5: Verify UI app**

Run:

```bash
(cd UnityNewsApp && ./gradlew :features:news:presentation:testDebugUnitTest)
(cd UnityNewsApp && ./gradlew :app:assembleDebug)
```

Expected: both commands pass.

- [ ] **Step 6: Commit**

```bash
git add UnityNewsApp
git commit -m "feat: add news reader presentation"
```

---

## Task 8: Release Hardening, READMEs, AI Disclosure, And Final Verification

**Files:**
- Modify: `UnityNewsApp/app/build.gradle.kts`
- Modify: `backend/app/build.gradle.kts`
- Modify: `UnityNewsApp/app/proguard-rules.pro`
- Modify: `backend/app/proguard-rules.pro`
- Create: `README.md`
- Create: `UnityNewsApp/README.md`
- Create: `backend/README.md`
- Modify: `docs/DESIGN.md`

- [ ] **Step 1: Enable release hardening**

In both app `build.gradle.kts` files, set release builds:

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

Add narrow ProGuard/R8 rules for AIDL parcelables:

```proguard
-keep class com.unitynews.contract.** implements android.os.Parcelable { *; }
-keepclassmembers class com.unitynews.contract.** {
    public static final android.os.Parcelable$Creator CREATOR;
}
```

- [ ] **Step 2: Write top-level README**

Create `README.md`:

```markdown
# Unity News

Unity News contains two independently installable Android applications:

- `UnityNewsApp`: focused offline-first news reader.
- `backend`: Android-native backend app that owns bundled articles, filtering, IPC, runtime status, and fault simulation.

## Architecture

The apps communicate over a versioned AIDL bound-service contract. The UI app renders cached data from Room and requests filtered article sets from the backend. The backend owns all filter execution and serves data from `backend/app/src/main/assets/articles.json`.

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

## Demo

1. Install backend APK.
2. Install UI APK.
3. Open backend app and start the foreground service.
4. Open UI app.
5. Apply title and rating filters.
6. Change backend scenarios and observe UI states.

## AI Tooling Disclosure

AI tooling was used as an engineering assistant for build diagnosis, architecture comparison, planning, and implementation support. Human review drove the final architecture decisions. One useful acceleration was identifying the AndroidX Core compile SDK mismatch. One correction was rejecting runtime Gist fetching and local HTTP after validating the requirement that the backend dataset must be bundled locally and that Android-native IPC better fits the security model.
```

- [ ] **Step 3: Run final verification**

Run:

```bash
./scripts/verify-aidl-contracts.sh
(cd UnityNewsApp && ./gradlew :app:assembleDebug)
(cd UnityNewsApp && ./gradlew :app:testDebugUnitTest)
(cd backend && ./gradlew :app:assembleDebug)
(cd backend && ./gradlew :app:testDebugUnitTest)
(cd UnityNewsApp && ./gradlew :app:assembleRelease)
(cd backend && ./gradlew :app:assembleRelease)
```

Expected: every command exits 0 with `BUILD SUCCESSFUL` for Gradle commands.

- [ ] **Step 4: Commit**

```bash
git add README.md UnityNewsApp backend docs scripts
git commit -m "docs: add review and release guidance"
```

---

## Final Acceptance Checklist

- [ ] Both APKs build independently.
- [ ] Backend app bundles `articles.json`; no runtime Gist fetch.
- [ ] UI app cannot read backend asset directly.
- [ ] Backend owns filtering.
- [ ] UI sends title/rating criteria and renders returned results.
- [ ] Filter specs come from backend.
- [ ] Rating options are derived from data.
- [ ] Room cache preserves content during backend failure.
- [ ] Backend operator console shows service state, scenario, article count, and request logs.
- [ ] Foreground service starts/stops explicitly.
- [ ] AIDL contracts are duplicated and verified.
- [ ] Signature permission and caller validation are implemented.
- [ ] Release builds use minification and resource shrinking.
- [ ] README explains build, install, test, demo, security, and AI tooling.
