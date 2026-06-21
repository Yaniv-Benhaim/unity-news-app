package com.unitynews.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Shared remote-image surface with a guaranteed color fallback behind it.
 *
 * Feature screens pass an already-sanitized image model. If it is null, the
 * placeholder still occupies the same space so lists do not jump while loading.
 */
@Composable
fun RemoteImageBox(
    imageModel: Any?,
    placeholderColor: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Box(
        modifier = modifier.background(placeholderColor),
    ) {
        imageModel?.let { model ->
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
