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

/**
 * Application entry point for the companion backend app.
 *
 * The backend app does not use Hilt yet; instead it initializes BackendRuntime
 * once with the application context.
 */
class UnityNewsBackendApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        BackendRuntime.initialize(this)
    }
}

/**
 * Small runtime container for backend singletons.
 *
 * This keeps the sample backend easy to inspect: the Android service, foreground
 * service, and console all share the same repository, scenario state, and logs.
 */
object BackendRuntime {
    private lateinit var applicationContext: Context

    val scenarioController = ScenarioController()
    val requestLogStore = RequestLogStore()
    val filterArticlesUseCase = FilterArticlesUseCase()
    val getFilterSpecsUseCase = GetFilterSpecsUseCase()

    private val _foregroundServiceRunning = MutableStateFlow(false)
    val foregroundServiceRunning: StateFlow<Boolean> = _foregroundServiceRunning.asStateFlow()

    /** Validates that only the trusted reader app can call the backend service. */
    val callerValidator: CallerValidator by lazy {
        CallerValidator(applicationContext)
    }

    /** Article source for this take-home backend. A real server could replace this. */
    val repository: ArticleRepository by lazy {
        AssetArticleRepository(applicationContext)
    }

    /** Store application context once so lazy runtime dependencies can be created. */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    /** Foreground service status is exposed to the visible console UI. */
    fun setForegroundServiceRunning(isRunning: Boolean) {
        _foregroundServiceRunning.value = isRunning
    }
}
