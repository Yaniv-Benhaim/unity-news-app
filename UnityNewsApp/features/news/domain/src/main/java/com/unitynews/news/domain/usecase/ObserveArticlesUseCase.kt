package com.unitynews.news.domain.usecase

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Reads articles for the current filter from the offline cache.
 *
 * This use case keeps the ViewModel from knowing that a repository exists.
 */
class ObserveArticlesUseCase(
    private val repository: NewsRepository,
) {
    operator fun invoke(criteria: FilterCriteria): Flow<List<Article>> =
        repository.observeArticles(criteria)
}
