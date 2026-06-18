package com.unitynews.news.data.aidl

import android.content.pm.PackageManager
import android.os.Build

fun interface PackageInspector {
    fun isPackageInstalled(packageName: String): Boolean
}

enum class BackendAvailability {
    Missing,
    Installed,
}

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
