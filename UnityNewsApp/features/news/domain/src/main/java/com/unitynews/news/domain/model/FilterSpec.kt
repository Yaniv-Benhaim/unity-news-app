package com.unitynews.news.domain.model

/**
 * Describes one filter the backend wants the reader app to render.
 *
 * The UI receives these specs dynamically, so adding a new backend filter does
 * not require adding a new composable for every possible filter key.
 */
data class FilterSpec(
    val key: String,
    val label: String,
    val type: FilterType,
    val options: List<String> = emptyList(),
)

/**
 * Filter input shapes currently supported by the UI.
 *
 * Unsupported is intentional: unknown backend filters should not crash the app.
 */
enum class FilterType {
    Text,
    MultiSelect,
    Unsupported,
}
