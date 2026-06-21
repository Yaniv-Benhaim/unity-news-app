package com.unitynews.server.domain.model

/**
 * Filter values sent by the reader app.
 *
 * Every filter travels as a backend-owned key with string values. The backend
 * decides which keys it understands and how those strings become domain rules.
 */
data class FilterCriteria @JvmOverloads constructor(
    val filterValues: Map<String, Set<String>> = emptyMap(),
)
