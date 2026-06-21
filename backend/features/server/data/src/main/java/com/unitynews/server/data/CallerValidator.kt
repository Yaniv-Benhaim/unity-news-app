package com.unitynews.server.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Process

/**
 * Validates callers before serving AIDL requests.
 *
 * The backend trusts its own process and the expected reader package only when
 * Android reports that both apps are signed with the same certificate.
 */
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

    /** Return true when the calling UID is allowed to use the backend API. */
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

/** Small wrapper around PackageManager so authorization logic can be tested. */
interface CallerPackageInspector {
    fun packagesForUid(uid: Int): List<String>
    fun hasSameSignature(packageName: String): Boolean
}

/** Default test-safe inspector: no external caller is trusted. */
private object NoExternalCallerPackageInspector : CallerPackageInspector {
    override fun packagesForUid(uid: Int): List<String> = emptyList()
    override fun hasSameSignature(packageName: String): Boolean = false
}

/** Production inspector backed by Android PackageManager. */
private class AndroidCallerPackageInspector(
    private val context: Context,
) : CallerPackageInspector {
    override fun packagesForUid(uid: Int): List<String> =
        context.packageManager.getPackagesForUid(uid)?.toList().orEmpty()

    override fun hasSameSignature(packageName: String): Boolean =
        context.packageManager.checkSignatures(context.packageName, packageName) ==
            PackageManager.SIGNATURE_MATCH
}
