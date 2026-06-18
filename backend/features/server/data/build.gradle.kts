plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

val parcelizeCompilerPlugin by configurations.creating

android {
    namespace = "com.unitynews.server.data"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        aidl = true
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            parcelizeCompilerPlugin.elements.map { files ->
                files.map { file -> "-Xplugin=${file.asFile.absolutePath}" }
            },
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("org.jetbrains.kotlin:kotlin-parcelize-runtime:${libs.versions.kotlin.get()}")
    parcelizeCompilerPlugin("org.jetbrains.kotlin:kotlin-parcelize-compiler:${libs.versions.kotlin.get()}")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
