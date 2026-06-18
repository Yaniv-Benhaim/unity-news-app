package com.unitynews.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unitynews.news.domain.model.Article

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
