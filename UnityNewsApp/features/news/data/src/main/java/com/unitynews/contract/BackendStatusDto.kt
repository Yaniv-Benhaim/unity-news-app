package com.unitynews.contract

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BackendStatusDto(
    val isRunning: Boolean,
    val scenario: String,
    val articleCount: Int,
) : Parcelable
