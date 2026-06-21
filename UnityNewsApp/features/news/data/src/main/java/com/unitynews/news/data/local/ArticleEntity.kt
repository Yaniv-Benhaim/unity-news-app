package com.unitynews.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unitynews.news.domain.model.Article

/**
 * Room row for an article.
 *
 * This is a data-layer shape. It intentionally stays separate from the domain
 * Article model so database concerns do not leak into business logic.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
    val lastFetchedAt: Long,
)

/** Convert a domain article into the database shape. */
internal fun Article.toEntity(lastFetchedAt: Long): ArticleEntity = ArticleEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    rating = rating,
    placeholderRed = placeholderRed,
    placeholderGreen = placeholderGreen,
    placeholderBlue = placeholderBlue,
    lastFetchedAt = lastFetchedAt,
)

/** Convert a database row back into the domain shape used by the UI. */
internal fun ArticleEntity.toDomain(): Article = Article(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    rating = rating,
    placeholderRed = placeholderRed,
    placeholderGreen = placeholderGreen,
    placeholderBlue = placeholderBlue,
)
