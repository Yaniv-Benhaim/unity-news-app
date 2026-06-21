package com.unitynews.server.presentation.model

import com.unitynews.server.domain.model.ServerScenario

/**
 * Complete state for the backend console screen.
 *
 * The screen receives this single object and does not need to know where the
 * data came from.
 */
data class BackendConsoleUiState(
    val isServiceRunning: Boolean = false,
    val scenario: ServerScenario = ServerScenario.Normal,
    val articleCount: Int = 0,
    val requestLogs: List<String> = emptyList(),
    val isRefreshing: Boolean = false,
)
