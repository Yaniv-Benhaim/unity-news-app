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
import com.unitynews.news.presentation.NewsScreen
import com.unitynews.news.presentation.NewsViewModel
import dagger.hilt.android.AndroidEntryPoint

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
                )
            }
        }
    }

    private fun openBackendSetup() {
        val packageName = "com.example.unitynewsbackend"
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
