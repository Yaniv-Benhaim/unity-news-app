package com.unitynews.news.domain.repository

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for news data.
 *
 * The domain layer owns this interface, while the data layer owns its
 * implementation. That lets us replace AIDL with HTTP later without changing
 * ViewModels or use cases.
 */
interface NewsRepository {
    /** Stream cached articles immediately and update whenever Room changes. */
    fun observeArticles(criteria: FilterCriteria): Flow<List<Article>>

    /** Ask the remote source for fresh data and update the local cache. */
    suspend fun refresh(criteria: FilterCriteria): Result<Unit>

    /** Ask the backend which filters the app should render. */
    suspend fun getFilterSpecs(): Result<List<FilterSpec>>
}
