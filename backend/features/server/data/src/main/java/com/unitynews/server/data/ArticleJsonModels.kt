package com.unitynews.server.data

import com.unitynews.server.domain.model.Article
import java.security.MessageDigest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Top-level shape of the articles asset file. */
@Serializable
data class ArticleJsonResponse(
    val articles: List<ArticleJsonModel>,
)

/** Raw JSON article shape before conversion into the domain Article model. */
@Serializable
data class ArticleJsonModel(
    val title: String,
    val description: String,
    @SerialName("image_url")
    val imageUrl: String,
    val rating: Int,
    val placeholderColor: PlaceholderColorJsonModel,
)

/** Placeholder color carried by the source data for image fallback UI. */
@Serializable
data class PlaceholderColorJsonModel(
    val red: Int,
    val green: Int,
    val blue: Int,
)

/**
 * Parses bundled JSON article data into backend domain models.
 *
 * Stable IDs are derived from title + image URL so the same article keeps the
 * same ID across app launches.
 */
object ArticleJsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun parse(rawJson: String): List<Article> =
        json.decodeFromString<ArticleJsonResponse>(rawJson)
            .articles
            .map { article ->
                Article(
                    id = stableId(article.title, article.imageUrl),
                    title = article.title,
                    description = article.description,
                    imageUrl = article.imageUrl,
                    rating = article.rating,
                    placeholderRed = article.placeholderColor.red,
                    placeholderGreen = article.placeholderColor.green,
                    placeholderBlue = article.placeholderColor.blue,
                )
    }

    /** SHA-256 keeps generated IDs deterministic and collision-resistant enough here. */
    private fun stableId(title: String, imageUrl: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((title + imageUrl).toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
