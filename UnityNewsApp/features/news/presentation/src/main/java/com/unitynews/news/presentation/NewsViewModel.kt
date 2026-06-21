package com.unitynews.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.usecase.GetFilterSpecsUseCase
import com.unitynews.news.domain.usecase.ObserveArticlesUseCase
import com.unitynews.news.domain.usecase.RefreshArticlesUseCase
import com.unitynews.news.presentation.model.NewsTextProvider
import com.unitynews.news.presentation.model.NewsUiState
import com.unitynews.news.presentation.model.readableMessage
import com.unitynews.news.presentation.model.withTextFilter
import com.unitynews.news.presentation.model.withToggledMultiSelectFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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

/**
 * Presentation state holder for the news screen.
 *
 * The ViewModel talks only to use cases. It does not know if articles come from
 * AIDL, HTTP, Room, or test fakes. Its job is to translate user actions and
 * domain results into one NewsUiState for Compose.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val observeArticles: ObserveArticlesUseCase,
    private val refreshArticles: RefreshArticlesUseCase,
    private val getFilterSpecs: GetFilterSpecsUseCase,
    private val textProvider: NewsTextProvider,
) : ViewModel() {
    /** Draft filter selection currently edited by the user. */
    private val mutableDraftCriteria = MutableStateFlow(FilterCriteria())
    val criteria: StateFlow<FilterCriteria> = mutableDraftCriteria.asStateFlow()

    /** Filter selection that has been applied and sent to the backend/cache. */
    private val appliedCriteria = MutableStateFlow(FilterCriteria())

    /** Dynamic filter definitions loaded from the backend. */
    private val filters = MutableStateFlow<List<FilterSpec>>(emptyList())
    /** Used by the UI to disable duplicate refresh taps. */
    private val isRefreshing = MutableStateFlow(false)
    /** Warning shown when refresh failed but cached articles can still be shown. */
    private val staleMessage = MutableStateFlow<String?>(null)
    /** True when backend calls fail in a way that suggests the backend is missing. */
    private val backendMissing = MutableStateFlow(false)
    /** Hard error from the local observation stream. */
    private val streamErrorMessage = MutableStateFlow<String?>(null)
    /** Bumped after refresh so the observed query restarts for the same criteria. */
    private val observeRevision = MutableStateFlow(0)
    /** Keeps old filter-loading jobs from overriding newer results. */
    private var filterLoadGeneration = 0

    /**
     * Observe cached articles for the current criteria.
     *
     * flatMapLatest is important: when the user changes filters, the old Room
     * stream is cancelled and only the newest filter selection drives the UI.
     */
    private val articles = combine(appliedCriteria, observeRevision) { criteria, _ -> criteria }
        .flatMapLatest { criteria ->
            streamErrorMessage.value = null
            observeArticles(criteria)
                .catch { error ->
                    streamErrorMessage.value = error.readableMessage(textProvider.unableToLoadArticles)
                    emit(emptyList<Article>())
                }
        }

    /** Small internal shape that groups the parts every feed-like state needs. */
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

    /**
     * Single public state consumed by Compose.
     *
     * The order matters: stream errors win first, real content wins over backend
     * missing, and backend missing wins over the empty state.
     */
    val state: StateFlow<NewsUiState> = combine(
        feedSnapshot,
        backendMissing,
        streamErrorMessage,
    ) { snapshot, backendMissing, streamErrorMessage ->
        when {
            streamErrorMessage != null -> NewsUiState.Error(
                message = streamErrorMessage,
                filters = snapshot.filters,
                isRefreshing = snapshot.isRefreshing,
                staleMessage = snapshot.staleMessage,
            )
            snapshot.articles.isNotEmpty() -> NewsUiState.Content(
                articles = snapshot.articles,
                filters = snapshot.filters,
                isRefreshing = snapshot.isRefreshing,
                staleMessage = snapshot.staleMessage,
            )
            backendMissing -> NewsUiState.BackendMissing(
                filters = snapshot.filters,
                isRefreshing = snapshot.isRefreshing,
                staleMessage = snapshot.staleMessage,
            )
            else -> NewsUiState.Empty(
                filters = snapshot.filters,
                isRefreshing = snapshot.isRefreshing,
                staleMessage = snapshot.staleMessage,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = NewsUiState.InitialLoading,
    )

    init {
        // Load filter metadata and article data as soon as the screen starts.
        loadFilterSpecs()
        refresh()
    }

    /**
     * Refresh articles from the backend for the current filters.
     *
     * Cached articles remain visible if refresh fails; the user sees a stale
     * message instead of losing the feed.
     */
    fun refresh() {
        if (isRefreshing.value) {
            return
        }
        isRefreshing.value = true
        viewModelScope.launch {
            staleMessage.value = null
            streamErrorMessage.value = null
            try {
                refreshArticles(appliedCriteria.value)
                    .onSuccess {
                        backendMissing.value = false
                        staleMessage.value = null
                        loadFilterSpecs()
                        observeRevision.value += 1
                    }
                    .onFailure { error ->
                        if (error is CancellationException) {
                            throw error
                        }
                        backendMissing.value = true
                        staleMessage.value = error.readableMessage(textProvider.unableToRefreshArticles)
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                backendMissing.value = true
                staleMessage.value = error.readableMessage(textProvider.unableToRefreshArticles)
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun updateTextFilter(key: String, value: String) {
        updateCriteria {
            it.withTextFilter(key, value)
        }
    }

    fun toggleMultiSelectFilter(key: String, option: String, selected: Boolean) {
        updateCriteria {
            it.withToggledMultiSelectFilter(key, option, selected)
        }
    }

    /**
     * Apply the draft filters as one request.
     *
     * This matches the assignment requirement: the user can edit title and
     * rating together, then send both filters to the backend with one action.
     */
    fun applyFilters() {
        val nextCriteria = mutableDraftCriteria.value
        if (nextCriteria != appliedCriteria.value) {
            streamErrorMessage.value = null
            staleMessage.value = null
            appliedCriteria.value = nextCriteria
            refresh()
        }
    }

    private fun updateCriteria(transform: (FilterCriteria) -> FilterCriteria) {
        val nextCriteria = transform(mutableDraftCriteria.value)
        if (nextCriteria != mutableDraftCriteria.value) {
            // Editing filters changes the draft only. The backend request waits
            // for Apply so multiple filter edits are sent together.
            streamErrorMessage.value = null
            staleMessage.value = null
            mutableDraftCriteria.value = nextCriteria
        }
    }

    private fun loadFilterSpecs() {
        val generation = ++filterLoadGeneration
        viewModelScope.launch {
            getFilterSpecs()
                .takeIf { generation == filterLoadGeneration }
                ?.let { result ->
                    applyFilterSpecsResult(result)
                }
        }
    }

    /** Apply backend filter metadata while keeping cancellation behavior correct. */
    private fun applyFilterSpecsResult(result: Result<List<FilterSpec>>) {
        result
            .onSuccess { specs ->
                filters.value = specs
                backendMissing.value = false
            }
            .onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                backendMissing.value = true
                staleMessage.value = error.readableMessage(textProvider.installOrStartBackend)
            }
    }
}

/** Internal grouping used before deciding the final NewsUiState. */
private data class FeedSnapshot(
    val articles: List<Article>,
    val filters: List<FilterSpec>,
    val isRefreshing: Boolean,
    val staleMessage: String?,
)
