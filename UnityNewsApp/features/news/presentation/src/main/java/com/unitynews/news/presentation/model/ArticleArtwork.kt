package com.unitynews.news.presentation.model

import com.unitynews.core.data.supportedRemoteUrlOrNull
import com.unitynews.news.domain.model.Article

/** UI-friendly artwork data derived from the Article model. */
internal data class ArticleArtwork(
    val red: Int,
    val green: Int,
    val blue: Int,
    val imageModel: String?,
)

/**
 * Prepare image data for the UI.
 *
 * The color is always safe to render. The image model is optional because image
 * URLs are external data and may be blank, malformed, expired, or unavailable.
 */
internal fun Article.toArticleArtwork(): ArticleArtwork =
    ArticleArtwork(
        red = placeholderRed.coerceIn(0, 255),
        green = placeholderGreen.coerceIn(0, 255),
        blue = placeholderBlue.coerceIn(0, 255),
        imageModel = imageUrl.supportedRemoteUrlOrNull(),
    )
