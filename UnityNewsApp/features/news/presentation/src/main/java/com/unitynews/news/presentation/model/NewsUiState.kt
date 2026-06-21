package com.unitynews.news.presentation.model

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterSpec

/**
 * All screen states the news reader can render.
 *
 * Keeping this as a sealed interface makes the UI exhaustive: when a new state
 * is added, the compiler helps us update the screen.
 */
sealed interface NewsUiState {
    /** Filters are shared by most states so controls can stay visible. */
    val filters: List<FilterSpec>
        get() = emptyList()
    /** True while a refresh request is currently running. */
    val isRefreshing: Boolean
        get() = false
    /** Non-blocking warning shown when cached content may be stale. */
    val staleMessage: String?
        get() = null

    /** First state before local cache and backend information have arrived. */
    data object InitialLoading : NewsUiState

    /** Normal state with articles to display. */
    data class Content(
        val articles: List<Article>,
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState

    /** Backend/cache responded, but the current filters have no articles. */
    data class Empty(
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState

    /** The reader cannot reach the companion backend app. */
    data class BackendMissing(
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState

    /** Something failed while observing the local article stream. */
    data class Error(
        val message: String,
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState
}
