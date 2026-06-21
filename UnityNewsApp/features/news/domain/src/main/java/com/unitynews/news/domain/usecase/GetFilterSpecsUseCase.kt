package com.unitynews.news.domain.usecase

import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.repository.NewsRepository

/**
 * Loads the filter definitions exposed by the backend.
 *
 * This is what lets the UI render backend-driven filters instead of hard-coded
 * frontend-only options.
 */
class GetFilterSpecsUseCase(
    private val repository: NewsRepository,
) {
    suspend operator fun invoke(): Result<List<FilterSpec>> =
        repository.getFilterSpecs()
}
