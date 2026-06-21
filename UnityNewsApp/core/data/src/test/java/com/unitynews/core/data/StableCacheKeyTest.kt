package com.unitynews.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StableCacheKeyTest {
    @Test
    fun `string set map hash is stable across key and value ordering`() {
        val first = mapOf(
            "topic" to linkedSetOf("unity", "android"),
            "region" to linkedSetOf("us", "eu"),
        )
        val reordered = mapOf(
            "region" to linkedSetOf("eu", "us"),
            "topic" to linkedSetOf("android", "unity"),
        )

        assertEquals(
            stableStringSetMapCacheKey(first),
            stableStringSetMapCacheKey(reordered),
        )
    }

    @Test
    fun `string set map hash trims empty values before encoding`() {
        val values = mapOf(
            " title " to setOf(" Unity ", "", "   "),
        )

        assertEquals(
            """{"title":["Unity"]}""",
            stableStringSetMapCacheKey(values),
        )
    }
}
