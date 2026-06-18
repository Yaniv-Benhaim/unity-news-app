package com.unitynews.news.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.model.FilterType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterControls(
    filters: List<FilterSpec>,
    criteria: FilterCriteria,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (filters.isEmpty()) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
    }
}

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
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private fun FilterCriteria.textValueFor(key: String): String =
    if (key == TITLE_FILTER_KEY) {
        titleQuery.orEmpty()
    } else {
        dynamicValues[key]?.firstOrNull().orEmpty()
    }

private fun FilterCriteria.isSelected(key: String, option: String): Boolean =
    if (key == RATING_FILTER_KEY) {
        option.toIntOrNull()?.let { ratingValues.contains(it) } ?: false
    } else {
        dynamicValues[key]?.contains(option) == true
    }

private const val TITLE_FILTER_KEY = "title"
private const val RATING_FILTER_KEY = "rating"
