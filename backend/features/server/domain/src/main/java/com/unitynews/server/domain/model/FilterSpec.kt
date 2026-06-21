package com.unitynews.server.domain.model

/**
 * Filter definition returned to the reader app.
 *
 * The reader renders these dynamically, so the backend can expose available
 * filter options without a frontend code change.
 */
data class FilterSpec(
    val key: String,
    val label: String,
    val type: FilterType,
    val options: List<String> = emptyList(),
)

/** Filter input shapes currently supported by the reader UI. */
enum class FilterType {
    Text,
    MultiSelect,
}
