package com.unitynews.news.presentation

import com.unitynews.news.domain.model.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArticleArtworkTest {
    @Test
    fun `article artwork uses local placeholder instead of remote image model`() {
        val article = article(
            imageUrl = "https://example.com/image.jpg",
            placeholderRed = 300,
            placeholderGreen = -10,
            placeholderBlue = 64,
        )

        val artwork = article.toArticleArtwork()

        assertEquals(255, artwork.red)
        assertEquals(0, artwork.green)
        assertEquals(64, artwork.blue)
        assertNull(artwork.imageModel)
    }
}

private fun article(
    imageUrl: String,
    placeholderRed: Int,
    placeholderGreen: Int,
    placeholderBlue: Int,
): Article =
    Article(
        id = "id",
        title = "Title",
        description = "Description",
        imageUrl = imageUrl,
        rating = 5,
        placeholderRed = placeholderRed,
        placeholderGreen = placeholderGreen,
        placeholderBlue = placeholderBlue,
    )
