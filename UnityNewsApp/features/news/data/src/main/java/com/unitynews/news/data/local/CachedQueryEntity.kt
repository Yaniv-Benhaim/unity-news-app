package com.unitynews.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.unitynews.core.data.stableStringSetMapCacheKey
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Stores the cached result for one filter selection.
 *
 * We cache by criteriaHash because each filter combination can have a different
 * article list. The article rows are stored separately in the articles table.
 */
@Entity(tableName = "cached_queries")
data class CachedQueryEntity(
    @PrimaryKey val criteriaHash: String,
    val articleIds: String,
    val lastSuccessfulRefreshAt: Long,
    val staleReason: String?,
)

/**
 * Builds a stable cache key for a filter selection.
 *
 * Sorting values is important: selecting "Tech then Gaming" should hit the same
 * cache entry as selecting "Gaming then Tech".
 */
internal fun FilterCriteria.toCriteriaHash(): String =
    stableStringSetMapCacheKey(filterValues)

/** Store article IDs as JSON so the query can preserve backend result order. */
internal fun encodeArticleIds(articleIds: List<String>): String =
    Json.encodeToString(articleIds)

/** Read the ordered article IDs saved for a cached query. */
internal fun CachedQueryEntity.decodeArticleIds(): List<String> =
    Json.decodeFromString(articleIds)
