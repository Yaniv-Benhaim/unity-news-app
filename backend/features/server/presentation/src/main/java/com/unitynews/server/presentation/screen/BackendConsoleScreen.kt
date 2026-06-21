package com.unitynews.server.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unitynews.backend.core.ui.ConsoleSection
import com.unitynews.backend.core.ui.StatusLine
import com.unitynews.server.domain.model.ServerScenario
import com.unitynews.server.presentation.R
import com.unitynews.server.presentation.model.BackendConsoleUiState

/**
 * Root screen for the backend control app.
 *
 * It exposes service controls, scenario controls, and recent request logs so a
 * reviewer can see how the backend behaves while the reader app is running.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackendConsoleScreen(
    uiState: BackendConsoleUiState,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onScenarioSelected: (ServerScenario) -> Unit,
    onRefreshStatus: () -> Unit,
    onClearLogs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.backend_console_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.backend_console_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Service section shows process/lifecycle state and foreground controls.
                ConsoleSection(title = stringResource(R.string.backend_console_section_service)) {
                    StatusLine(
                        label = stringResource(R.string.backend_console_label_foreground),
                        value = stringResource(
                            if (uiState.isServiceRunning) {
                                R.string.backend_console_value_running
                            } else {
                                R.string.backend_console_value_stopped
                            },
                        ),
                    )
                    StatusLine(
                        label = stringResource(R.string.backend_console_label_scenario),
                        value = uiState.scenario.displayName(),
                    )
                    StatusLine(
                        label = stringResource(R.string.backend_console_label_articles),
                        value = uiState.articleCount.toString(),
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onStartService,
                            enabled = !uiState.isServiceRunning,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(stringResource(R.string.backend_console_action_start))
                        }
                        OutlinedButton(
                            onClick = onStopService,
                            enabled = uiState.isServiceRunning,
                        ) {
                            Text(stringResource(R.string.backend_console_action_stop))
                        }
                        OutlinedButton(onClick = onRefreshStatus) {
                            Text(
                                text = stringResource(
                                    if (uiState.isRefreshing) {
                                        R.string.backend_console_action_refreshing
                                    } else {
                                        R.string.backend_console_action_refresh
                                    },
                                ),
                            )
                        }
                    }
                }

                // Scenario section lets the reviewer force success/error/empty/slow responses.
                ConsoleSection(title = stringResource(R.string.backend_console_section_scenarios)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ServerScenario.entries.forEach { scenario ->
                            FilterChip(
                                selected = scenario == uiState.scenario,
                                onClick = { onScenarioSelected(scenario) },
                                label = { Text(scenario.displayName()) },
                            )
                        }
                    }
                }

                // Request logs are useful proof that reader/backend IPC is happening.
                ConsoleSection(title = stringResource(R.string.backend_console_section_request_logs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.backend_console_request_log_count,
                                uiState.requestLogs.size,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(onClick = onClearLogs) {
                            Text(stringResource(R.string.backend_console_action_clear))
                        }
                    }

                    if (uiState.requestLogs.isEmpty()) {
                        Text(
                            text = stringResource(R.string.backend_console_no_requests),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.requestLogs.forEach { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerScenario.displayName(): String =
    stringResource(
        when (this) {
            ServerScenario.Normal -> R.string.backend_console_scenario_normal
            ServerScenario.Unauthorized -> R.string.backend_console_scenario_unauthorized
            ServerScenario.ServerError -> R.string.backend_console_scenario_server_error
            ServerScenario.Empty -> R.string.backend_console_scenario_empty
            ServerScenario.Slow -> R.string.backend_console_scenario_slow
        },
    )

@Preview(showBackground = true)
@Composable
private fun BackendConsoleScreenPreview() {
    MaterialTheme {
        BackendConsoleScreen(
            uiState = BackendConsoleUiState(
                isServiceRunning = true,
                scenario = ServerScenario.Slow,
                articleCount = 24,
                requestLogs = listOf(
                    "[12:00:01] request=demo criteria=title=*,ratings=* scenario=Normal result=24 articles duration=18ms",
                ),
            ),
            onStartService = {},
            onStopService = {},
            onScenarioSelected = {},
            onRefreshStatus = {},
            onClearLogs = {},
        )
    }
}
