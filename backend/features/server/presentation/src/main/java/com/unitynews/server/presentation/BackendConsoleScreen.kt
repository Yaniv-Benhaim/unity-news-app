package com.unitynews.server.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unitynews.server.domain.model.ServerScenario

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
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
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
                Text(
                    text = "Backend Console",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                ConsoleSection(title = "Service") {
                    StatusLine(label = "Foreground", value = if (uiState.isServiceRunning) "Running" else "Stopped")
                    StatusLine(label = "Scenario", value = uiState.scenario.name)
                    StatusLine(label = "Articles", value = uiState.articleCount.toString())

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = onStartService,
                            enabled = !uiState.isServiceRunning,
                        ) {
                            Text("Start")
                        }
                        OutlinedButton(
                            onClick = onStopService,
                            enabled = uiState.isServiceRunning,
                        ) {
                            Text("Stop")
                        }
                        OutlinedButton(onClick = onRefreshStatus) {
                            Text(if (uiState.isRefreshing) "Refreshing" else "Refresh")
                        }
                    }
                }

                ConsoleSection(title = "Scenarios") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ServerScenario.entries.forEach { scenario ->
                            FilterChip(
                                selected = scenario == uiState.scenario,
                                onClick = { onScenarioSelected(scenario) },
                                label = { Text(scenario.name) },
                            )
                        }
                    }
                }

                ConsoleSection(title = "Request Logs") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${uiState.requestLogs.size} entries",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedButton(onClick = onClearLogs) {
                            Text("Clear")
                        }
                    }

                    if (uiState.requestLogs.isEmpty()) {
                        Text(
                            text = "No requests yet",
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
private fun ConsoleSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        content()
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider()
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

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
