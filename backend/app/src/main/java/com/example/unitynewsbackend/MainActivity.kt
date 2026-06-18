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
import com.unitynews.server.presentation.BackendConsoleScreen
import com.unitynews.server.presentation.BackendConsoleViewModel

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
                        viewModel.recordServiceAction("started")
                        ContextCompat.startForegroundService(
                            this,
                            NewsBackendForegroundService.intent(this),
                        )
                    },
                    onStopService = {
                        viewModel.recordServiceAction("stopped")
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
