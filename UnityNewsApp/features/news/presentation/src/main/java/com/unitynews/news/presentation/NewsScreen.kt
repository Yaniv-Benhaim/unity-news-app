package com.unitynews.news.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    state: NewsUiState,
    criteria: FilterCriteria,
    onRefresh: () -> Unit,
    onOpenBackendSetup: () -> Unit,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshing = state.isRefreshing
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Unity News") },
                actions = {
                    TextButton(
                        onClick = onRefresh,
                        enabled = !isRefreshing,
                    ) {
                        Text(if (isRefreshing) "Refreshing" else "Refresh")
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

@Composable
private fun ArticleFeed(
    state: NewsUiState.Content,
    criteria: FilterCriteria,
    onRefresh: () -> Unit,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FilterControls(
                filters = state.filters,
                criteria = criteria,
                onTextFilterChanged = onTextFilterChanged,
                onMultiSelectFilterChanged = onMultiSelectFilterChanged,
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

@Composable
private fun ArticleCard(article: Article) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArticleImage(article = article)
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Rating ${article.rating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ArticleImage(article: Article) {
    val placeholderColor = Color(
        red = article.placeholderRed.coerceIn(0, 255),
        green = article.placeholderGreen.coerceIn(0, 255),
        blue = article.placeholderBlue.coerceIn(0, 255),
    )
    Box(
        modifier = Modifier
            .size(width = 88.dp, height = 72.dp)
            .clip(MaterialTheme.shapes.small)
            .background(placeholderColor),
    ) {
        AsyncImage(
            model = article.imageUrl.takeIf { it.isNotBlank() },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun StaleMessage(
    message: String,
    onRefresh: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onRefresh) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(
    state: NewsUiState.Empty,
    criteria: FilterCriteria,
    onRefresh: () -> Unit,
    onTextFilterChanged: (key: String, value: String) -> Unit,
    onMultiSelectFilterChanged: (key: String, option: String, selected: Boolean) -> Unit,
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
            )
        }
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "No articles",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Try refreshing or changing the filters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = onRefresh,
                    enabled = !state.isRefreshing,
                ) {
                    Text(if (state.isRefreshing) "Refreshing" else "Refresh")
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    state: NewsUiState.Error,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = "Could not load news",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRefresh,
            enabled = !state.isRefreshing,
        ) {
            Text(if (state.isRefreshing) "Retrying" else "Retry")
        }
    }
}
