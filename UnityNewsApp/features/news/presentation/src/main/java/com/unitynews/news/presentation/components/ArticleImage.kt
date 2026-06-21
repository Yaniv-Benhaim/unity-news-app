package com.unitynews.news.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unitynews.core.ui.RemoteImageBox
import com.unitynews.news.domain.model.Article
import com.unitynews.news.presentation.model.toArticleArtwork

/** Remote article image with a stable color fallback behind it. */
@Composable
internal fun ArticleImage(article: Article) {
    val artwork = article.toArticleArtwork()
    val placeholderColor = Color(
        red = artwork.red,
        green = artwork.green,
        blue = artwork.blue,
    )
    RemoteImageBox(
        imageModel = artwork.imageModel,
        placeholderColor = placeholderColor,
        contentDescription = article.title,
        modifier = Modifier
            .size(width = 104.dp, height = 88.dp)
            .clip(MaterialTheme.shapes.small),
    )
}
