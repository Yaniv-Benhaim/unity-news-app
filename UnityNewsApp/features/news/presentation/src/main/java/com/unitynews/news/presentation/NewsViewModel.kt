package com.unitynews.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.repository.NewsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(
    private val repository: NewsRepository,
    initialCriteria: FilterCriteria = FilterCriteria(),
) : ViewModel() {
    private val mutableCriteria = MutableStateFlow(initialCriteria)
    val criteria: StateFlow<FilterCriteria> = mutableCriteria.asStateFlow()

    private val filters = MutableStateFlow<List<FilterSpec>>(emptyList())
    private val isRefreshing = MutableStateFlow(false)
    private val staleMessage = MutableStateFlow<String?>(null)
    private val backendMissing = MutableStateFlow(false)
    private val streamErrorMessage = MutableStateFlow<String?>(null)

    private val articles = mutableCriteria
        .flatMapLatest { criteria ->
            repository.observeArticles(criteria)
                .catch { error ->
                    streamErrorMessage.value = error.readableMessage("Unable to load articles")
                    emit(emptyList<Article>())
                }
        }

    private val feedSnapshot = combine(
        articles,
        filters,
        isRefreshing,
        staleMessage,
    ) { articles, filters, isRefreshing, staleMessage ->
        FeedSnapshot(
            articles = articles,
            filters = filters,
            isRefreshing = isRefreshing,
            staleMessage = staleMessage,
        )
    }

    val state: StateFlow<NewsUiState> = combine(
        feedSnapshot,
        backendMissing,
        streamErrorMessage,
    ) { snapshot, backendMissing, streamErrorMessage ->
        when {
            streamErrorMessage != null -> NewsUiState.Error(streamErrorMessage)
            snapshot.articles.isNotEmpty() -> NewsUiState.Content(
                articles = snapshot.articles,
                filters = snapshot.filters,
                isRefreshing = snapshot.isRefreshing,
                staleMessage = snapshot.staleMessage,
            )
            backendMissing -> NewsUiState.BackendMissing
            else -> NewsUiState.Empty
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewsUiState.InitialLoading,
    )

    init {
        loadFilterSpecs()
    }

    fun refresh() {
        viewModelScope.launch {
            isRefreshing.value = true
            staleMessage.value = null
            repository.refresh(mutableCriteria.value)
                .onSuccess {
                    backendMissing.value = false
                }
                .onFailure { error ->
                    if (error is CancellationException) {
                        throw error
                    }
                    backendMissing.value = true
                    staleMessage.value = error.readableMessage("Unable to refresh articles")
                }
            isRefreshing.value = false
        }
    }

    fun updateTextFilter(key: String, value: String) {
        mutableCriteria.value = mutableCriteria.value.updateTextFilter(key, value)
    }

    fun toggleMultiSelectFilter(key: String, option: String, selected: Boolean) {
        mutableCriteria.value = mutableCriteria.value.updateMultiSelectFilter(key, option, selected)
    }

    private fun loadFilterSpecs() {
        viewModelScope.launch {
            repository.getFilterSpecs()
                .onSuccess { specs ->
                    filters.value = specs
                    backendMissing.value = false
                }
                .onFailure { error ->
                    if (error is CancellationException) {
                        throw error
                    }
                    backendMissing.value = true
                    staleMessage.value = error.readableMessage("Install or start the Unity News backend")
                }
        }
    }
}

private fun FilterCriteria.updateTextFilter(key: String, value: String): FilterCriteria {
    val normalizedValue = value.trim().takeIf { it.isNotEmpty() }
    return if (key == TITLE_FILTER_KEY) {
        copy(titleQuery = normalizedValue)
    } else {
        copy(dynamicValues = dynamicValues.withSingleDynamicValue(key, normalizedValue))
    }
}

private fun FilterCriteria.updateMultiSelectFilter(
    key: String,
    option: String,
    selected: Boolean,
): FilterCriteria =
    if (key == RATING_FILTER_KEY) {
        val rating = option.toIntOrNull() ?: return this
        copy(
            ratingValues = if (selected) {
                ratingValues + rating
            } else {
                ratingValues - rating
            },
        )
    } else {
        copy(dynamicValues = dynamicValues.withToggledDynamicValue(key, option, selected))
    }

private fun Map<String, Set<String>>.withSingleDynamicValue(
    key: String,
    value: String?,
): Map<String, Set<String>> {
    val updated = toMutableMap()
    if (value == null) {
        updated -= key
    } else {
        updated[key] = setOf(value)
    }
    return updated
}

private fun Map<String, Set<String>>.withToggledDynamicValue(
    key: String,
    option: String,
    selected: Boolean,
): Map<String, Set<String>> {
    val updated = toMutableMap()
    val values = updated[key].orEmpty()
    val nextValues = if (selected) values + option else values - option
    if (nextValues.isEmpty()) {
        updated -= key
    } else {
        updated[key] = nextValues
    }
    return updated
}

private fun Throwable.readableMessage(fallback: String): String =
    message?.takeIf { it.isNotBlank() } ?: fallback

private data class FeedSnapshot(
    val articles: List<Article>,
    val filters: List<FilterSpec>,
    val isRefreshing: Boolean,
    val staleMessage: String?,
)

private const val TITLE_FILTER_KEY = "title"
private const val RATING_FILTER_KEY = "rating"
