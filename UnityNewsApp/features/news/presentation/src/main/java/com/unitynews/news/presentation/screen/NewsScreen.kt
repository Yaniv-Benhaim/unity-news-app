package com.unitynews.news.presentation.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.presentation.R
import com.unitynews.news.presentation.components.EmptyContent
import com.unitynews.news.presentation.components.ErrorContent
import com.unitynews.news.presentation.components.LoadingContent
import com.unitynews.news.presentation.model.NewsUiState

/**
 * Root composable for the reader screen.
 *
 * The root owns only scaffold/routing. Feature-specific pieces live in
 * screen/components/filters packages so each file has one reason to change.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    state: NewsUiState,
    criteria: FilterCriteria,
    onRefresh: () -> Unit,
    onOpenBackendSetup: () -> Unit,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    onApplyFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshing = state.isRefreshing
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.news_screen_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.news_screen_subtitle),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                    ) {
                        Text(
                            text = stringResource(
                                if (isRefreshing) {
                                    R.string.news_action_refreshing
                                } else {
                                    R.string.news_action_refresh
                                },
                            ),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            NewsUiState.InitialLoading -> LoadingContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            is NewsUiState.Content -> ArticleFeed(
                state = state,
                criteria = criteria,
                onRefresh = onRefresh,
                onTextFilterChanged = onTextFilterChanged,
                onMultiSelectFilterChanged = onMultiSelectFilterChanged,
                onApplyFilters = onApplyFilters,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            is NewsUiState.Empty -> EmptyContent(
                state = state,
                criteria = criteria,
                onRefresh = onRefresh,
                onTextFilterChanged = onTextFilterChanged,
                onMultiSelectFilterChanged = onMultiSelectFilterChanged,
                onApplyFilters = onApplyFilters,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            is NewsUiState.BackendMissing -> BackendSetupScreen(
                onOpenBackendSetup = onOpenBackendSetup,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.padding(innerPadding),
            )

            is NewsUiState.Error -> ErrorContent(
                state = state,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        }
    }
}
