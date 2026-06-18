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
    val normalizedValues = linkedMapOf(
        "title" to listOf(titleQuery?.trim().orEmpty()),
        "ratings" to ratingValues.sorted().map { it.toString() },
    )
    dynamicValues.toSortedMap().forEach { (key, values) ->
        normalizedValues["dynamic:${key.trim()}"] = values.map { it.trim() }.sorted()
    }
    return Json.encodeToString(normalizedValues)
}

internal fun encodeArticleIds(articleIds: List<String>): String =
    Json.encodeToString(articleIds)

internal fun CachedQueryEntity.decodeArticleIds(): List<String> =
    Json.decodeFromString(articleIds)
