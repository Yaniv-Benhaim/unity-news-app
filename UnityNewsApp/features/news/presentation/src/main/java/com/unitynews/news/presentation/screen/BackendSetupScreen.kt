package com.unitynews.news.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unitynews.news.presentation.R

/**
 * Shown when the reader cannot reach the companion backend app.
 *
 * The button delegates to the Activity because opening Play Store/installer
 * flows is platform navigation, not screen-rendering logic.
 */
@Composable
fun BackendSetupScreen(
    onOpenBackendSetup: () -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(24.dp),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.news_backend_required_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRefreshing) {
                        stringResource(R.string.news_backend_required_checking)
                    } else {
                        stringResource(R.string.news_backend_required_message)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = onOpenBackendSetup) {
                    Text(stringResource(R.string.news_action_open_backend_setup))
                }
            }
        }
    }
}
