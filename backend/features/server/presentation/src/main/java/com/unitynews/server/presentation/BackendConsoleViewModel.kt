package com.unitynews.server.presentation

import com.unitynews.server.data.RequestLogStore
import com.unitynews.server.data.ScenarioController
import com.unitynews.server.domain.model.ServerScenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackendConsoleViewModel(
    private val scenarioController: ScenarioController,
    private val requestLogStore: RequestLogStore,
    private val serviceRunning: StateFlow<Boolean>,
    private val articleCountProvider: suspend () -> Int,
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val articleCount = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(BackendConsoleUiState())

    val uiState: StateFlow<BackendConsoleUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            combine(
                serviceRunning,
                scenarioController.scenario,
                articleCount,
                requestLogStore.logs,
                isRefreshing,
            ) { isRunning, scenario, count, logs, refreshing ->
                BackendConsoleUiState(
                    isServiceRunning = isRunning,
                    scenario = scenario,
                    articleCount = count,
                    requestLogs = logs,
                    isRefreshing = refreshing,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
        refreshStatus()
    }

    fun setScenario(scenario: ServerScenario) {
        scenarioController.setScenario(scenario)
    }

    fun clearLogs() {
        requestLogStore.clear()
    }

    fun refreshStatus() {
        scope.launch {
            isRefreshing.value = true
            try {
                runCatching {
                    withContext(Dispatchers.IO) {
                        articleCountProvider()
                    }
                }.onSuccess { count ->
                    articleCount.value = count
                }.onFailure { error ->
                    requestLogStore.add("console refresh failed: ${error.message ?: "unknown error"}")
                }
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun recordServiceAction(action: String) {
        requestLogStore.add("operator $action foreground service")
    }

    override fun close() {
        scope.cancel()
    }
}
