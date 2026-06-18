package com.unitynews.contract

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArticleDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
) : Parcelable
