package com.unitynews.news.presentation.model

import com.unitynews.news.domain.model.FilterCriteria

/**
 * Immutable editing helpers for the filter draft shown on the news screen.
 *
 * The ViewModel uses these helpers so it can describe user intent instead of
 * exposing the Map/Set mechanics used by generic backend-provided filters.
 */
fun FilterCriteria.withTextFilter(key: String, value: String): FilterCriteria {
    val normalizedValue = value.trim().takeIf { it.isNotEmpty() }
    return copy(filterValues = filterValues.withSingleFilterValue(key, normalizedValue))
}

/**
 * Return a new FilterCriteria with one multi-select option added or removed.
 */
fun FilterCriteria.withToggledMultiSelectFilter(
    key: String,
    option: String,
    selected: Boolean,
): FilterCriteria =
    copy(filterValues = filterValues.withToggledFilterValue(key, option, selected))

/**
 * Text filters store exactly one value for a backend-provided filter key.
 *
 * Passing null removes the key, which means this filter is no longer active.
 */
private fun Map<String, Set<String>>.withSingleFilterValue(
    key: String,
    value: String?,
): Map<String, Set<String>> {
    val updated = toMutableMap()
    if (value == null) {
        updated -= key
    } else {
        updated[key] = setOf(value)
    }
    return updated
}

/**
 * Multi-select filters store zero or more selected values for one backend key.
 *
 * Empty selections remove the key entirely so the backend receives only active
 * filters.
 */
private fun Map<String, Set<String>>.withToggledFilterValue(
    key: String,
    option: String,
    selected: Boolean,
): Map<String, Set<String>> {
    val updated = toMutableMap()
    val values = updated[key].orEmpty()
    val nextValues = if (selected) values + option else values - option

    if (nextValues.isEmpty()) {
        updated -= key
    } else {
        updated[key] = nextValues
    }
    return updated
}
