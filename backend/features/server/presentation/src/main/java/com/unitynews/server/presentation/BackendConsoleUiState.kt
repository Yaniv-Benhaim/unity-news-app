package com.unitynews.server.presentation

import com.unitynews.server.domain.model.ServerScenario

data class BackendConsoleUiState(
    val isServiceRunning: Boolean = false,
    val scenario: ServerScenario = ServerScenario.Normal,
    val articleCount: Int = 0,
    val requestLogs: List<String> = emptyList(),
    val isRefreshing: Boolean = false,
)
