package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class OfflineFirstNewsRepository(
    private val local: NewsLocalDataSource,
    private val remote: RemoteArticleDataSource,
) : NewsRepository {
    override fun observeArticles(criteria: FilterCriteria): Flow<List<Article>> =
        local.observe(criteria)

    override suspend fun refresh(criteria: FilterCriteria): Result<Unit> =
        remote.getArticles(criteria).fold(
            onSuccess = { articles ->
                local.replace(criteria, articles)
                Result.success(Unit)
            },
            onFailure = { error ->
                local.markStale(criteria, error.message ?: "Remote refresh failed")
                Result.failure(error)
            },
        )

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> =
        remote.getFilterSpecs()
}
