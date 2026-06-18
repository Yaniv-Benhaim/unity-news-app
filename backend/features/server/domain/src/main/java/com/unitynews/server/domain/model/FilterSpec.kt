package com.unitynews.server.domain.model

data class FilterSpec(
    val key: String,
    val label: String,
    val type: FilterType,
    val options: List<String> = emptyList(),
)

enum class FilterType {
    Text,
    MultiSelect,
}
