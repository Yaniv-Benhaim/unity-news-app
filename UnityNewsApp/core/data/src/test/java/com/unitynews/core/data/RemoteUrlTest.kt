package com.unitynews.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteUrlTest {
    @Test
    fun `supported remote url accepts http and https after trimming`() {
        assertEquals("https://example.com/image.jpg", " https://example.com/image.jpg ".supportedRemoteUrlOrNull())
        assertEquals("http://example.com/image.jpg", "http://example.com/image.jpg".supportedRemoteUrlOrNull())
    }

    @Test
    fun `supported remote url rejects blank and local file values`() {
        assertEquals(null, "   ".supportedRemoteUrlOrNull())
        assertEquals(null, "file:///tmp/image.jpg".supportedRemoteUrlOrNull())
    }
}
