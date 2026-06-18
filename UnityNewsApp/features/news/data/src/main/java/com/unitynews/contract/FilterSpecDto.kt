package com.unitynews.contract

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilterSpecDto(
    val key: String,
    val label: String,
    val type: String,
    val options: List<String>,
) : Parcelable
