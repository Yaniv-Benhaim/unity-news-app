package com.unitynews.contract

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArticleFilterRequestTest {
    @Test
    fun parcelRoundTripPreservesDynamicValues() {
        val request = ArticleFilterRequest(
            titleQuery = "unity",
            ratingValues = listOf(5, 3),
            requestId = "request-1",
            dynamicValues = mapOf(
                "section" to listOf("sports", "finance"),
                "region" to listOf("eu"),
            ),
        )
        val parcel = Parcel.obtain()

        request.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val decoded = ArticleFilterRequest.CREATOR.createFromParcel(parcel)
        parcel.recycle()

        assertEquals(request.titleQuery, decoded.titleQuery)
        assertEquals(request.ratingValues, decoded.ratingValues)
        assertEquals(request.requestId, decoded.requestId)
        assertEquals(request.dynamicValues, decoded.dynamicValues)
    }
}
