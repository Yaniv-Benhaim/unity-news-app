package com.unitynews.contract

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArticleFilterRequestTest {
    @Test
    fun parcelRoundTripPreservesFilterValues() {
        val request = ArticleFilterRequest(
            requestId = "request-1",
            filterValues = mapOf(
                "title" to listOf("unity"),
                "rating" to listOf("5", "3"),
                "section" to listOf("sports", "finance"),
                "region" to listOf("eu"),
            ),
        )
        val parcel = Parcel.obtain()

        request.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val decoded = ArticleFilterRequest.CREATOR.createFromParcel(parcel)
        parcel.recycle()

        assertEquals(request.requestId, decoded.requestId)
        assertEquals(request.filterValues, decoded.filterValues)
    }
}
