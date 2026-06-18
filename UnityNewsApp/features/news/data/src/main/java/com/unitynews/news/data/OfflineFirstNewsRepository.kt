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
                runCatching { local.replace(criteria, articles) }
            },
            onFailure = { error ->
                runCatching { local.markStale(criteria, error.message ?: "Remote refresh failed") }
                    .fold(
                        onSuccess = { Result.failure(error) },
                        onFailure = { localError -> Result.failure(localError) },
                    )
            },
        )

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> =
        remote.getFilterSpecs()
}
