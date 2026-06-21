package com.unitynews.core.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Builds a deterministic cache key from backend-driven filter values.
 *
 * Sorting keys and values keeps cache lookups stable even when the UI toggles
 * chips in a different order.
 */
fun stableStringSetMapCacheKey(values: Map<String, Set<String>>): String {
    val normalizedValues = linkedMapOf<String, List<String>>()
    values.toSortedMap().forEach { (key, entryValues) ->
        normalizedValues[key.trim()] = entryValues
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .sorted()
    }
    return Json.encodeToString(normalizedValues)
}
