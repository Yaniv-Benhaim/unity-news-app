package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterCriteria

class FilterArticlesUseCase {

    operator fun invoke(
        articles: List<Article>,
        criteria: FilterCriteria,
    ): List<Article> {
        val titleQuery = criteria.titleQuery?.trim()?.takeIf { it.isNotEmpty() }
        val ratingValues = criteria.ratingValues

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
