package com.example.unitynewsbackend

import android.app.Application
import android.content.Context
import com.unitynews.server.data.AssetArticleRepository
import com.unitynews.server.data.CallerValidator
import com.unitynews.server.data.RequestLogStore
import com.unitynews.server.data.ScenarioController
import com.unitynews.server.domain.repository.ArticleRepository
import com.unitynews.server.domain.usecase.FilterArticlesUseCase
import com.unitynews.server.domain.usecase.GetFilterSpecsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnityNewsBackendApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BackendRuntime.initialize(this)
    }
}

object BackendRuntime {
    private lateinit var applicationContext: Context

    val scenarioController = ScenarioController()
    val requestLogStore = RequestLogStore()
    val filterArticlesUseCase = FilterArticlesUseCase()
    val getFilterSpecsUseCase = GetFilterSpecsUseCase()

    private val _foregroundServiceRunning = MutableStateFlow(false)
    val foregroundServiceRunning: StateFlow<Boolean> = _foregroundServiceRunning.asStateFlow()

    val callerValidator: CallerValidator by lazy {
        CallerValidator(applicationContext)
    }

    val repository: ArticleRepository by lazy {
        AssetArticleRepository(applicationContext)
    }

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun setForegroundServiceRunning(isRunning: Boolean) {
        _foregroundServiceRunning.value = isRunning
    }
}
