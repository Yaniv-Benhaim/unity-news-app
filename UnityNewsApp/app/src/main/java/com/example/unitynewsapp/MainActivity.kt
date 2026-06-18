package com.example.unitynewsapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.unitynewsapp.ui.theme.UnityNewsAppTheme
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.presentation.NewsScreen
import com.unitynews.news.presentation.NewsUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnityNewsAppTheme {
                NewsScreen(
                    state = NewsUiState.BackendMissing,
                    criteria = FilterCriteria(),
                    onRefresh = {},
                    onOpenBackendSetup = ::openBackendSetup,
                    onTextFilterChanged = { _, _ -> },
                    onMultiSelectFilterChanged = { _, _, _ -> },
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
