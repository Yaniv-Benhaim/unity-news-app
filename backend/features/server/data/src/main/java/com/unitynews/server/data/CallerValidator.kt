package com.unitynews.server.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Process

class CallerValidator(
    private val backendUid: Int = Process.myUid(),
    private val allowedPackageName: String = DEFAULT_ALLOWED_PACKAGE_NAME,
    private val packageInspector: CallerPackageInspector = NoExternalCallerPackageInspector,
) {
    constructor(
        context: Context,
        backendUid: Int = Process.myUid(),
        allowedPackageName: String = DEFAULT_ALLOWED_PACKAGE_NAME,
    ) : this(
        backendUid = backendUid,
        allowedPackageName = allowedPackageName,
        packageInspector = AndroidCallerPackageInspector(context.applicationContext),
    )

    fun validate(callingUid: Int): Boolean {
        if (callingUid == backendUid) return true

        val callerPackages = packageInspector.packagesForUid(callingUid)
        return allowedPackageName in callerPackages &&
            packageInspector.hasSameSignature(allowedPackageName)
    }

    companion object {
        const val DEFAULT_ALLOWED_PACKAGE_NAME = "com.example.unitynewsapp"
    }
}

interface CallerPackageInspector {
    fun packagesForUid(uid: Int): List<String>
    fun hasSameSignature(packageName: String): Boolean
}

private object NoExternalCallerPackageInspector : CallerPackageInspector {
    override fun packagesForUid(uid: Int): List<String> = emptyList()
    override fun hasSameSignature(packageName: String): Boolean = false
}

private class AndroidCallerPackageInspector(
    private val context: Context,
) : CallerPackageInspector {
    override fun packagesForUid(uid: Int): List<String> =
        context.packageManager.getPackagesForUid(uid)?.toList().orEmpty()

    override fun hasSameSignature(packageName: String): Boolean =
        context.packageManager.checkSignatures(context.packageName, packageName) ==
            PackageManager.SIGNATURE_MATCH
}
