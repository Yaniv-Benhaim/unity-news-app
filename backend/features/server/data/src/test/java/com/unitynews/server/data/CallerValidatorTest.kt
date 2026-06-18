package com.unitynews.server.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallerValidatorTest {

    @Test
    fun validateAllowsBackendUid() {
        val validator = CallerValidator(
            backendUid = 1000,
            packageInspector = FakeCallerPackageInspector(),
        )

        assertTrue(validator.validate(1000))
    }

    @Test
    fun validateAllowsConfiguredUiPackageWhenSignedWithBackendSignature() {
        val validator = CallerValidator(
            backendUid = 1000,
            packageInspector = FakeCallerPackageInspector(
                packagesByUid = mapOf(2000 to listOf("com.example.unitynewsapp")),
                sameSignaturePackages = setOf("com.example.unitynewsapp"),
            ),
        )

        assertTrue(validator.validate(2000))
    }

    @Test
    fun validateRejectsConfiguredUiPackageWhenSignatureDoesNotMatch() {
        val validator = CallerValidator(
            backendUid = 1000,
            packageInspector = FakeCallerPackageInspector(
                packagesByUid = mapOf(2000 to listOf("com.example.unitynewsapp")),
            ),
        )

        assertFalse(validator.validate(2000))
    }

    @Test
    fun validateRejectsUnknownPackageEvenWhenSignatureMatches() {
        val validator = CallerValidator(
            backendUid = 1000,
            packageInspector = FakeCallerPackageInspector(
                packagesByUid = mapOf(2000 to listOf("com.example.otherapp")),
                sameSignaturePackages = setOf("com.example.otherapp"),
            ),
        )

        assertFalse(validator.validate(2000))
    }

    private class FakeCallerPackageInspector(
        private val packagesByUid: Map<Int, List<String>> = emptyMap(),
        private val sameSignaturePackages: Set<String> = emptySet(),
    ) : CallerPackageInspector {
        override fun packagesForUid(uid: Int): List<String> = packagesByUid[uid].orEmpty()

        override fun hasSameSignature(packageName: String): Boolean =
            packageName in sameSignaturePackages
    }
}
