package com.unitynews.news.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.presentation.R
import com.unitynews.news.presentation.filters.FilterControls
import com.unitynews.news.presentation.model.NewsUiState

/** Empty feed state that still keeps filters visible. */
@Composable
internal fun EmptyContent(
    state: NewsUiState.Empty,
    criteria: FilterCriteria,
    onRefresh: () -> Unit,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            FilterControls(
                filters = state.filters,
                criteria = criteria,
                onTextFilterChanged = onTextFilterChanged,
                onMultiSelectFilterChanged = onMultiSelectFilterChanged,
                onApplyFilters = onApplyFilters,
            )
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = stringResource(R.string.news_empty_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.news_empty_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = onRefresh,
                        enabled = !state.isRefreshing,
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isRefreshing) {
                                    R.string.news_action_refreshing
                                } else {
                                    R.string.news_action_refresh
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}
