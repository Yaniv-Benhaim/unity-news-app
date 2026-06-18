package com.unitynews.server.domain.usecase

import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterSpec
import com.unitynews.server.domain.model.FilterType

class GetFilterSpecsUseCase {

    operator fun invoke(articles: List<Article>): List<FilterSpec> {
        val ratingOptions = articles
            .map { it.rating }
            .distinct()
            .sorted()
            .map { it.toString() }

        return listOf(
            FilterSpec(key = "title", label = "Title", type = FilterType.Text),
            FilterSpec(
                key = "rating",
                label = "Rating",
                type = FilterType.MultiSelect,
                options = ratingOptions,
            ),
        )
    }
}
