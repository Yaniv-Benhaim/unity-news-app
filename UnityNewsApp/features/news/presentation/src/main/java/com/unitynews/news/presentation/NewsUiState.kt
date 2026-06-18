package com.unitynews.news.presentation

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterSpec

sealed interface NewsUiState {
    val filters: List<FilterSpec>
        get() = emptyList()
    val isRefreshing: Boolean
        get() = false
    val staleMessage: String?
        get() = null

    data object InitialLoading : NewsUiState

    data class Content(
        val articles: List<Article>,
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState

    data class Empty(
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState

    data class BackendMissing(
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState

    data class Error(
        val message: String,
        override val filters: List<FilterSpec>,
        override val isRefreshing: Boolean,
        override val staleMessage: String?,
    ) : NewsUiState
}
