package com.unitynews.news.data.aidl

import android.content.pm.PackageManager
import android.os.Build

/** Small testable wrapper around PackageManager. */
fun interface PackageInspector {
    fun isPackageInstalled(packageName: String): Boolean
}

/** Reader-side view of whether the backend app is present on the device. */
enum class BackendAvailability {
    Missing,
    Installed,
}

/**
 * Checks if the companion backend package is installed.
 *
 * The reader can use this to guide the user to install or open the backend.
 */
class BackendAvailabilityChecker(
    private val packageInspector: PackageInspector,
    private val backendPackageName: String = DEFAULT_BACKEND_PACKAGE,
) {
    fun check(): BackendAvailability =
        if (packageInspector.isPackageInstalled(backendPackageName)) {
            BackendAvailability.Installed
        } else {
            BackendAvailability.Missing
        }

    companion object {
        const val DEFAULT_BACKEND_PACKAGE = "com.example.unitynewsbackend"
    }
}

/** Production PackageInspector backed by Android PackageManager. */
class AndroidPackageInspector(
    private val packageManager: PackageManager,
) : PackageInspector {
    override fun isPackageInstalled(packageName: String): Boolean =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
}
