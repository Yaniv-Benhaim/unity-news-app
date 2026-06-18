package com.unitynews.server.data

import com.unitynews.server.domain.model.Article
import java.security.MessageDigest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ArticleJsonResponse(
    val articles: List<ArticleJsonModel>,
)

@Serializable
data class ArticleJsonModel(
    val title: String,
    val description: String,
    @SerialName("image_url")
    val imageUrl: String,
    val rating: Int,
    val placeholderColor: PlaceholderColorJsonModel,
)

@Serializable
data class PlaceholderColorJsonModel(
    val red: Int,
    val green: Int,
    val blue: Int,
)

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

    private fun stableId(title: String, imageUrl: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest((title + imageUrl).toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
