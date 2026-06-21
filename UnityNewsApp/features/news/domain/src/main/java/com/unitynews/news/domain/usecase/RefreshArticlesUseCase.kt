package com.unitynews.news.domain.usecase

import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.repository.NewsRepository

/**
 * Refreshes cached articles for the current filter.
 *
 * The repository decides how to combine remote data and local storage; the
 * ViewModel only needs to know whether the refresh succeeded.
 */
class RefreshArticlesUseCase(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(criteria: FilterCriteria): Result<Unit> =
        repository.refresh(criteria)
}
