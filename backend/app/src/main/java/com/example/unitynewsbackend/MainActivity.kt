package com.example.unitynewsbackend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import com.example.unitynewsbackend.service.NewsBackendForegroundService
import com.example.unitynewsbackend.ui.theme.UnityNewsBackendTheme
import com.unitynews.server.presentation.BackendConsoleViewModel
import com.unitynews.server.presentation.screen.BackendConsoleScreen

/**
 * Visible control app for the backend.
 *
 * This Activity lets a reviewer start/stop the foreground service, switch
 * backend scenarios, and inspect recent reader requests.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UnityNewsBackendTheme {
                val viewModel = remember {
                    BackendConsoleViewModel(
                        scenarioController = BackendRuntime.scenarioController,
                        requestLogStore = BackendRuntime.requestLogStore,
                        serviceRunning = BackendRuntime.foregroundServiceRunning,
                        articleCountProvider = { BackendRuntime.repository.getArticles().size },
                    )
                }
                val uiState by viewModel.uiState.collectAsState()

                DisposableEffect(viewModel) {
                    onDispose { viewModel.close() }
                }

                BackendConsoleScreen(
                    uiState = uiState,
                    onStartService = {
                        viewModel.recordServiceAction(
                            getString(R.string.backend_operator_started_foreground_service),
                        )
                        // A foreground service makes the backend explicit and long-running.
                        ContextCompat.startForegroundService(
                            this,
                            NewsBackendForegroundService.intent(this),
                        )
                    },
                    onStopService = {
                        viewModel.recordServiceAction(
                            getString(R.string.backend_operator_stopped_foreground_service),
                        )
                        stopService(NewsBackendForegroundService.intent(this))
                    },
                    onScenarioSelected = viewModel::setScenario,
                    onRefreshStatus = viewModel::refreshStatus,
                    onClearLogs = viewModel::clearLogs,
                )
            }
        }
    }
}
