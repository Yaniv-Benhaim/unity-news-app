package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterCriteria

/**
 * Applies reader-provided filters to the backend article list.
 *
 * This is intentionally pure Kotlin so filtering rules are easy to unit test.
 */
class FilterArticlesUseCase {

    operator fun invoke(
        articles: List<Article>,
        criteria: FilterCriteria,
    ): List<Article> {
        val titleQuery = criteria.filterValues[TITLE_FILTER_KEY]
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val ratingValues = criteria.filterValues[RATING_FILTER_KEY]
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()

        // No active filters means the backend should return the full feed.
        if (titleQuery == null && ratingValues.isEmpty()) {
            return articles
        }

        return articles.filter { article ->
            val matchesTitle = titleQuery == null ||
                article.title.contains(titleQuery, ignoreCase = true)
            val matchesRating = ratingValues.isEmpty() || article.rating in ratingValues

            matchesTitle && matchesRating
        }
    }
}

private const val TITLE_FILTER_KEY = "title"
private const val RATING_FILTER_KEY = "rating"
