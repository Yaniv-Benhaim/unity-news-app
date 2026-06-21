package com.unitynews.news.presentation


import com.unitynews.news.domain.model.Article
import com.unitynews.news.presentation.model.toArticleArtwork
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleArtworkTest {
    @Test
    fun `article artwork uses valid remote image model and clamps placeholder color`() {
        val imageUrl = "https://example.com/image.jpg"
        val article = article(
            imageUrl = imageUrl,
            placeholderRed = 300,
            placeholderGreen = -10,
            placeholderBlue = 64,
        )

        val artwork = article.toArticleArtwork()

        assertEquals(255, artwork.red)
        assertEquals(0, artwork.green)
        assertEquals(64, artwork.blue)
        assertEquals(imageUrl, artwork.imageModel)
    }

    @Test
    fun `article artwork ignores blank and unsupported image urls`() {
        val blankArtwork = article(imageUrl = "   ").toArticleArtwork()
        val fileArtwork = article(imageUrl = "file:///tmp/image.jpg").toArticleArtwork()

        assertEquals(null, blankArtwork.imageModel)
        assertEquals(null, fileArtwork.imageModel)
    }
}

private fun article(
    imageUrl: String = "https://example.com/image.jpg",
    placeholderRed: Int = 12,
    placeholderGreen: Int = 34,
    placeholderBlue: Int = 56,
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
