package com.unitynews.news.presentation

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterSpec

sealed interface NewsUiState {
    data object InitialLoading : NewsUiState

    data class Content(
        val articles: List<Article>,
        val filters: List<FilterSpec>,
        val isRefreshing: Boolean,
        val staleMessage: String?,
    ) : NewsUiState

    data object Empty : NewsUiState

    data object BackendMissing : NewsUiState

    data class Error(
        val message: String,
    ) : NewsUiState
}
