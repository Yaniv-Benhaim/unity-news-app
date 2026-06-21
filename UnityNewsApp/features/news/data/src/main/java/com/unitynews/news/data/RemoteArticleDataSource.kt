package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec

/**
 * Remote source contract for the news feature.
 *
 * Today this is implemented with Android native IPC. A future production server
 * could provide another implementation backed by Retrofit/HTTP.
 */
interface RemoteArticleDataSource {
    /** Fetch fresh articles for the selected filters. */
    suspend fun getArticles(criteria: FilterCriteria): Result<List<Article>>

    /** Fetch filter definitions so the UI can stay backend-driven. */
    suspend fun getFilterSpecs(): Result<List<FilterSpec>>
}
