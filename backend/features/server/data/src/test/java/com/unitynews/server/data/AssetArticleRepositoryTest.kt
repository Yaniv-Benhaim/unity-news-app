package com.unitynews.server.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetArticleRepositoryTest {

    @Test
    fun parseMapsJsonArticlesToDomainArticles() {
        val json = """
            {
              "articles": [
                {
                  "title": "Unity News Launches",
                  "description": "Backend service goes live.",
                  "image_url": "https://example.com/news.png",
                  "rating": 5,
                  "placeholderColor": {
                    "red": 12,
                    "green": 34,
                    "blue": 56
                  }
                }
              ]
            }
        """.trimIndent()

        val articles = ArticleJsonParser.parse(json)

        assertEquals(1, articles.size)
        assertEquals(
            "e223ab2636d6916cb061d817a8356ffc28fad236ea2e71f5f29ef2b8844fefc1",
            articles.single().id,
        )
        assertEquals("Unity News Launches", articles.single().title)
        assertEquals("Backend service goes live.", articles.single().description)
        assertEquals("https://example.com/news.png", articles.single().imageUrl)
        assertEquals(5, articles.single().rating)
        assertEquals(12, articles.single().placeholderRed)
        assertEquals(34, articles.single().placeholderGreen)
        assertEquals(56, articles.single().placeholderBlue)
    }

    @Test
    fun parseReturnsEmptyListWhenArticlesArrayIsEmpty() {
        val articles = ArticleJsonParser.parse("""{"articles":[]}""")

        assertTrue(articles.isEmpty())
    }
}
