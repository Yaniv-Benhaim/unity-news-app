package com.unitynews.contract

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArticleFilterRequest(
    val titleQuery: String?,
    val ratingValues: List<Int>,
    val requestId: String,
) : Parcelable
