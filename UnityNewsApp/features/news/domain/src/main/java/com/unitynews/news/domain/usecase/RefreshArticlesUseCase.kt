package com.unitynews.news.domain.usecase

import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.repository.NewsRepository

class RefreshArticlesUseCase(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(criteria: FilterCriteria): Result<Unit> =
        repository.refresh(criteria)
}
