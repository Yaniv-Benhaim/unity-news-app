package com.unitynews.news.domain.model

/**
 * User-selected filters for the article feed.
 *
 * The reader app keeps this generic on purpose. Keys such as "title" and
 * "rating" come from backend-provided FilterSpec objects; the UI stores the
 * selected values without knowing how each key is interpreted.
 */
data class FilterCriteria @JvmOverloads constructor(
    val filterValues: Map<String, Set<String>> = emptyMap(),
)
