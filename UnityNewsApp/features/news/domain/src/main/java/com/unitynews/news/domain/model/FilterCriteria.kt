package com.unitynews.news.domain.model

data class FilterCriteria @JvmOverloads constructor(
    val titleQuery: String? = null,
    val ratingValues: Set<Int> = emptySet(),
    val dynamicValues: Map<String, Set<String>> = emptyMap(),
)
