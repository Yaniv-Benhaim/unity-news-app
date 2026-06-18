package com.unitynews.news.domain.model

data class FilterCriteria(
    val titleQuery: String? = null,
    val ratingValues: Set<Int> = emptySet(),
)
