package com.unitynews.server.domain.model

/**
 * Article as the backend domain layer understands it.
 *
 * This is separate from AIDL DTOs so the backend can change internal data
 * loading without accidentally changing the cross-app contract.
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
