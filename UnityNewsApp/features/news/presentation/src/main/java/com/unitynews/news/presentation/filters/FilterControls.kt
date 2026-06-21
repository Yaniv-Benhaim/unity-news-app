package com.unitynews.news.presentation.filters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.model.FilterType
import com.unitynews.news.presentation.R

/**
 * Renders backend-provided filter definitions.
 *
 * The UI switches on FilterType, not on hard-coded filter keys, so most new
 * backend filters can appear without a frontend code change.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterControls(
    filters: List<FilterSpec>,
    criteria: FilterCriteria,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filters.isEmpty()) {
        // No filter metadata yet, so avoid reserving empty space.
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = stringResource(R.string.news_filters_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.news_filters_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            filters.forEach { filter ->
                when (filter.type) {
                    FilterType.Text -> TextFilter(
                        filter = filter,
                        value = criteria.textValueFor(filter.key),
                        onValueChanged = { value -> onTextFilterChanged(filter.key, value) },
                    )

                    FilterType.MultiSelect -> MultiSelectFilter(
                        filter = filter,
                        criteria = criteria,
                        onSelectionChanged = onMultiSelectFilterChanged,
                    )

                    FilterType.Unsupported -> UnsupportedFilter(filter = filter)
                }
            }

            Button(
                onClick = onApplyFilters,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(stringResource(R.string.news_action_apply_filters))
            }
        }
    }
}

/** Render a single-line text input for text filters. */
@Composable
private fun TextFilter(
    filter: FilterSpec,
    value: String,
    onValueChanged: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(filter.label) },
        supportingText = { Text(stringResource(R.string.news_filter_text_support)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Render chip options for multi-select filters. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiSelectFilter(
    filter: FilterSpec,
    criteria: FilterCriteria,
    onSelectionChanged: (key: String, option: String, selected: Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = filter.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            filter.options.forEach { option ->
                val selected = criteria.isSelected(filter.key, option)
                FilterChip(
                    selected = selected,
                    onClick = {
                        onSelectionChanged(filter.key, option, !selected)
                    },
                    label = { Text(option) },
                )
            }
        }
    }
}

/**
 * Unknown filter types are visible but disabled.
 *
 * This is safer than crashing if the backend ships a filter type the app has
 * not learned how to render yet.
 */
@Composable
private fun UnsupportedFilter(filter: FilterSpec) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = false,
            enabled = false,
            onClick = {},
            label = { Text(filter.label) },
        )
    }
}

/** Read the current text value for either a built-in or dynamic text filter. */
private fun FilterCriteria.textValueFor(key: String): String =
    filterValues[key]?.firstOrNull().orEmpty()

/** Check if a chip is selected for either a built-in or dynamic multi-select filter. */
private fun FilterCriteria.isSelected(key: String, option: String): Boolean =
    filterValues[key]?.contains(option) == true
