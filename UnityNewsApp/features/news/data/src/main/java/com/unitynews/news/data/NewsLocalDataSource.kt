package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.flow.Flow

/**
 * Local storage contract for the news feature.
 *
 * The repository depends on this interface instead of depending directly on
 * Room. That makes the cache easy to test and replace.
 */
interface NewsLocalDataSource {
    /** Watch cached results for one exact filter selection. */
    fun observe(criteria: FilterCriteria): Flow<List<Article>>

    /** Replace the cached result for one filter after a successful refresh. */
    suspend fun replace(criteria: FilterCriteria, articles: List<Article>)

    /** Remember that a refresh failed while keeping any older cached data. */
    suspend fun markStale(criteria: FilterCriteria, reason: String)
}
