package com.unitynews.news.domain.model

/**
 * Article as the reader feature understands it.
 *
 * This model belongs to the domain layer, so it does not know if the article
 * came from Room, AIDL, HTTP, or a test fake.
 */
data class Article(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
)
