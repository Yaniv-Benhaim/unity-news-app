package com.unitynews.news.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.presentation.components.ArticleCard
import com.unitynews.news.presentation.components.StaleMessage
import com.unitynews.news.presentation.filters.FilterControls
import com.unitynews.news.presentation.model.NewsUiState

/** List of articles plus filters and stale-cache messaging. */
@Composable
internal fun ArticleFeed(
    state: NewsUiState.Content,
    criteria: FilterCriteria,
    onRefresh: () -> Unit,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
        state.staleMessage?.let { message ->
            item {
                StaleMessage(message = message, onRefresh = onRefresh)
            }
        }
        items(
            items = state.articles,
            key = { article -> article.id },
        ) { article ->
            ArticleCard(article = article)
        }
    }
}
