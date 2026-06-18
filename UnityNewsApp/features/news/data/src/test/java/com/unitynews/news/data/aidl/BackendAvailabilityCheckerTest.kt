package com.unitynews.news.data.aidl

import org.junit.Assert.assertEquals
import org.junit.Test

class BackendAvailabilityCheckerTest {
    @Test
    fun `missing package maps to missing availability`() {
        val checker = BackendAvailabilityChecker(
            packageInspector = FakePackageInspector(installedPackages = emptySet()),
        )

        val availability = checker.check()

        assertEquals(BackendAvailability.Missing, availability)
    }

    @Test
    fun `installed package maps to installed availability`() {
        val checker = BackendAvailabilityChecker(
            packageInspector = FakePackageInspector(
                installedPackages = setOf("com.example.unitynewsbackend"),
            ),
        )

        val availability = checker.check()

        assertEquals(BackendAvailability.Installed, availability)
    }
}

private class FakePackageInspector(
    private val installedPackages: Set<String>,
) : PackageInspector {
    override fun isPackageInstalled(packageName: String): Boolean =
        packageName in installedPackages
}
