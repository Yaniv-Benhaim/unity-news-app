package com.example.unitynewsbackend.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import com.example.unitynewsbackend.BackendRuntime
import com.unitynews.contract.ArticleDto
import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.BackendStatusDto
import com.unitynews.contract.IArticlesCallback
import com.unitynews.contract.IBackendStatusCallback
import com.unitynews.contract.IFilterSpecsCallback
import com.unitynews.contract.INewsBackendService
import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.model.FilterCriteria
import com.unitynews.server.domain.model.FilterSpec
import com.unitynews.server.domain.model.ServerScenario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewsBackendService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val binder = object : INewsBackendService.Stub() {
        override fun getApiVersion(): Int = API_VERSION

        override fun getFilterSpecs(callback: IFilterSpecsCallback) {
            val callingUid = Binder.getCallingUid()
            serviceScope.launch {
                if (!validateCaller(callingUid, callback::onError)) return@launch

                runCatching {
                    val articles = BackendRuntime.repository.getArticles()
                    BackendRuntime.getFilterSpecsUseCase(articles).map { it.toDto() }
                }.onSuccess(callback::onSuccess)
                    .onFailure { error ->
                        callback.onError("SERVER_ERROR", error.message ?: "Unable to load filters")
                    }
            }
        }

        override fun getArticles(request: ArticleFilterRequest, callback: IArticlesCallback) {
            val callingUid = Binder.getCallingUid()
            val startedAt = SystemClock.elapsedRealtime()
            serviceScope.launch {
                val scenario = BackendRuntime.scenarioController.scenario.value
                val criteria = request.toCriteria()
                val requestLabel = "request=${request.requestId.ifBlank { "unlabeled" }}"

                if (!validateCaller(callingUid, callback::onError)) {
                    logArticlesRequest(requestLabel, criteria, scenario, "UNAUTHORIZED", startedAt)
                    return@launch
                }

                when (scenario) {
                    ServerScenario.Unauthorized -> {
                        callback.onError("UNAUTHORIZED", "Caller is not authorized")
                        logArticlesRequest(requestLabel, criteria, scenario, "UNAUTHORIZED", startedAt)
                    }

                    ServerScenario.ServerError -> {
                        callback.onError("SERVER_ERROR", "Scenario forced a server error")
                        logArticlesRequest(requestLabel, criteria, scenario, "SERVER_ERROR", startedAt)
                    }

                    ServerScenario.Empty -> {
                        callback.onSuccess(emptyList())
                        logArticlesRequest(requestLabel, criteria, scenario, "0 articles", startedAt)
                    }

                    ServerScenario.Slow -> {
                        delay(SLOW_SCENARIO_DELAY_MS)
                        returnFilteredArticles(callback, requestLabel, criteria, scenario, startedAt)
                    }

                    ServerScenario.Normal -> {
                        returnFilteredArticles(callback, requestLabel, criteria, scenario, startedAt)
                    }
                }
            }
        }

        override fun getBackendStatus(callback: IBackendStatusCallback) {
            val callingUid = Binder.getCallingUid()
            serviceScope.launch {
                if (!validateCaller(callingUid, callback::onError)) return@launch

                runCatching {
                    BackendStatusDto(
                        isRunning = true,
                        scenario = BackendRuntime.scenarioController.scenario.value.name,
                        articleCount = BackendRuntime.repository.getArticles().size,
                    )
                }.onSuccess(callback::onSuccess)
                    .onFailure { error ->
                        callback.onError("SERVER_ERROR", error.message ?: "Unable to load status")
                    }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun returnFilteredArticles(
        callback: IArticlesCallback,
        requestLabel: String,
        criteria: FilterCriteria,
        scenario: ServerScenario,
        startedAt: Long,
    ) {
        runCatching {
            val articles = BackendRuntime.repository.getArticles()
            BackendRuntime.filterArticlesUseCase(articles, criteria).map { it.toDto() }
        }.onSuccess { articles ->
            callback.onSuccess(articles)
            logArticlesRequest(requestLabel, criteria, scenario, "${articles.size} articles", startedAt)
        }.onFailure { error ->
            callback.onError("SERVER_ERROR", error.message ?: "Unable to load articles")
            logArticlesRequest(requestLabel, criteria, scenario, "SERVER_ERROR", startedAt)
        }
    }

    private fun logArticlesRequest(
        requestLabel: String,
        criteria: FilterCriteria,
        scenario: ServerScenario,
        result: String,
        startedAt: Long,
    ) {
        val durationMs = SystemClock.elapsedRealtime() - startedAt
        BackendRuntime.requestLogStore.add(
            "$requestLabel criteria=${criteria.toLogString()} scenario=${scenario.name} result=$result duration=${durationMs}ms",
        )
    }

    private fun validateCaller(
        callingUid: Int,
        onError: (String, String) -> Unit,
    ): Boolean {
        val isValid = BackendRuntime.callerValidator.validate(callingUid)
        if (!isValid) {
            onError("UNAUTHORIZED", "Caller is not authorized")
        }
        return isValid
    }

    private fun ArticleFilterRequest.toCriteria(): FilterCriteria =
        FilterCriteria(
            titleQuery = titleQuery,
            ratingValues = ratingValues.toSet(),
        )

    private fun Article.toDto(): ArticleDto =
        ArticleDto(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            rating = rating,
            placeholderRed = placeholderRed,
            placeholderGreen = placeholderGreen,
            placeholderBlue = placeholderBlue,
        )

    private fun FilterSpec.toDto(): com.unitynews.contract.FilterSpecDto =
        com.unitynews.contract.FilterSpecDto(
            key = key,
            label = label,
            type = type.name,
            options = options,
        )

    private fun FilterCriteria.toLogString(): String =
        "title=${titleQuery?.takeIf { it.isNotBlank() } ?: "*"},ratings=${ratingValues.sorted().ifEmpty { listOf("*") }}"

    private companion object {
        const val API_VERSION = 1
        const val SLOW_SCENARIO_DELAY_MS = 800L
    }
}
