package com.example.unitynewsapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.unitynewsapp.ui.theme.UnityNewsAppTheme
import com.unitynews.news.presentation.NewsViewModel
import com.unitynews.news.presentation.screen.NewsScreen
import dagger.hilt.android.AndroidEntryPoint

/**
 * Entry point for the reader app.
 *
 * The Activity stays intentionally thin: it collects state from the ViewModel
 * and forwards user actions back to the ViewModel. This keeps UI rendering,
 * business rules, caching, and backend communication out of the Activity.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnityNewsAppTheme {
                val state by viewModel.state.collectAsState()
                val criteria by viewModel.criteria.collectAsState()

                NewsScreen(
                    state = state,
                    criteria = criteria,
                    onRefresh = viewModel::refresh,
                    onOpenBackendSetup = ::openBackendSetup,
                    onTextFilterChanged = viewModel::updateTextFilter,
                    onMultiSelectFilterChanged = viewModel::toggleMultiSelectFilter,
                    onApplyFilters = viewModel::applyFilters,
                )
            }
        }
    }

    private fun openBackendSetup() {
        val packageName = "com.example.unitynewsbackend"
        // Prefer the Play Store app when it exists, then fall back to the web page.
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName"),
        )
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName"),
        )
        try {
            startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            startActivity(webIntent)
        }
    }
}
