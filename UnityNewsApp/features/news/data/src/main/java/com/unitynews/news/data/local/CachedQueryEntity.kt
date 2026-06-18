package com.unitynews.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "cached_queries")
data class CachedQueryEntity(
    @PrimaryKey val criteriaHash: String,
    val articleIds: String,
    val lastSuccessfulRefreshAt: Long,
    val staleReason: String?,
)

internal fun FilterCriteria.toCriteriaHash(): String {
    val normalizedTitle = titleQuery?.trim().orEmpty()
    val normalizedRatings = ratingValues.sorted().joinToString(separator = ",")
    return "title=$normalizedTitle|ratings=$normalizedRatings"
}

internal fun encodeArticleIds(articleIds: List<String>): String =
    Json.encodeToString(articleIds)

internal fun CachedQueryEntity.decodeArticleIds(): List<String> =
    Json.decodeFromString(articleIds)
