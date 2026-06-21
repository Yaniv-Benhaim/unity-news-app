package com.unitynews.server.presentation

import com.unitynews.server.data.RequestLogStore
import com.unitynews.server.data.ScenarioController
import com.unitynews.server.domain.model.ServerScenario
import com.unitynews.server.presentation.model.BackendConsoleUiState
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

/**
 * State holder for the visible backend console.
 *
 * This class is not an Android ViewModel because it is manually created by the
 * backend Activity from the shared BackendRuntime objects.
 */
class BackendConsoleViewModel(
    private val scenarioController: ScenarioController,
    private val requestLogStore: RequestLogStore,
    private val serviceRunning: StateFlow<Boolean>,
    private val articleCountProvider: suspend () -> Int,
) : AutoCloseable {
    /** Manual coroutine scope because this object is manually closed by the Activity. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val articleCount = MutableStateFlow(0)
    private val isRefreshing = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(BackendConsoleUiState())

    val uiState: StateFlow<BackendConsoleUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            // Merge runtime state into one UI state object for Compose.
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

    /** Change how the AIDL service will respond to future article requests. */
    fun setScenario(scenario: ServerScenario) {
        scenarioController.setScenario(scenario)
    }

    /** Remove all visible request log entries. */
    fun clearLogs() {
        requestLogStore.clear()
    }

    /** Refresh console-only status such as current article count. */
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

    /** Record operator actions so service start/stop is visible in the console log. */
    fun recordServiceAction(message: String) {
        requestLogStore.add(message)
    }

    /** Cancel work when the Activity composition leaves. */
    override fun close() {
        scope.cancel()
    }
}
