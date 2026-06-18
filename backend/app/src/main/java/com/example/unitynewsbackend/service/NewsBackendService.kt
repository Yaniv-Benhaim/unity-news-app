package com.example.unitynewsbackend.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import com.example.unitynewsbackend.BackendRuntime
import com.unitynews.contract.ArticleDto
import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.BackendStatusDto
import com.unitynews.contract.FilterSpecDto
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
                if (!validateCaller(callingUid)) {
                    safeFilterSpecsError(callback, "UNAUTHORIZED", "Caller is not authorized")
                    return@launch
                }

                runCatching {
                    val articles = BackendRuntime.repository.getArticles()
                    BackendRuntime.getFilterSpecsUseCase(articles).map { it.toDto() }
                }.onSuccess { specs ->
                    safeFilterSpecsSuccess(callback, specs)
                }
                    .onFailure { error ->
                        safeFilterSpecsError(
                            callback,
                            "SERVER_ERROR",
                            error.message ?: "Unable to load filters",
                        )
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

                if (!validateCaller(callingUid)) {
                    val callbackSent = safeArticleError(
                        callback,
                        "UNAUTHORIZED",
                        "Caller is not authorized",
                    )
                    logArticlesRequest(
                        requestLabel,
                        criteria,
                        scenario,
                        articleLogResult("UNAUTHORIZED", callbackSent),
                        startedAt,
                    )
                    return@launch
                }

                when (scenario) {
                    ServerScenario.Unauthorized -> {
                        val callbackSent = safeArticleError(
                            callback,
                            "UNAUTHORIZED",
                            "Caller is not authorized",
                        )
                        logArticlesRequest(
                            requestLabel,
                            criteria,
                            scenario,
                            articleLogResult("UNAUTHORIZED", callbackSent),
                            startedAt,
                        )
                    }

                    ServerScenario.ServerError -> {
                        val callbackSent = safeArticleError(
                            callback,
                            "SERVER_ERROR",
                            "Scenario forced a server error",
                        )
                        logArticlesRequest(
                            requestLabel,
                            criteria,
                            scenario,
                            articleLogResult("SERVER_ERROR", callbackSent),
                            startedAt,
                        )
                    }

                    ServerScenario.Empty -> {
                        val callbackSent = safeArticleSuccess(callback, emptyList())
                        logArticlesRequest(
                            requestLabel,
                            criteria,
                            scenario,
                            articleLogResult("0 articles", callbackSent),
                            startedAt,
                        )
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
                if (!validateCaller(callingUid)) {
                    safeStatusError(callback, "UNAUTHORIZED", "Caller is not authorized")
                    return@launch
                }

                runCatching {
                    BackendStatusDto(
                        isRunning = true,
                        scenario = BackendRuntime.scenarioController.scenario.value.name,
                        articleCount = BackendRuntime.repository.getArticles().size,
                    )
                }.onSuccess { status ->
                    safeStatusSuccess(callback, status)
                }
                    .onFailure { error ->
                        safeStatusError(
                            callback,
                            "SERVER_ERROR",
                            error.message ?: "Unable to load status",
                        )
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
            val callbackSent = safeArticleSuccess(callback, articles)
            logArticlesRequest(
                requestLabel,
                criteria,
                scenario,
                articleLogResult("${articles.size} articles", callbackSent),
                startedAt,
            )
        }.onFailure { error ->
            val callbackSent = safeArticleError(
                callback,
                "SERVER_ERROR",
                error.message ?: "Unable to load articles",
            )
            logArticlesRequest(
                requestLabel,
                criteria,
                scenario,
                articleLogResult("SERVER_ERROR", callbackSent),
                startedAt,
            )
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

    private fun safeArticleSuccess(callback: IArticlesCallback, articles: List<ArticleDto>): Boolean =
        runCallback("articles success") {
            callback.onSuccess(articles)
        }

    private fun safeArticleError(callback: IArticlesCallback, code: String, message: String): Boolean =
        runCallback("articles error $code") {
            callback.onError(code, message)
        }

    private fun safeFilterSpecsSuccess(callback: IFilterSpecsCallback, specs: List<FilterSpecDto>): Boolean =
        runCallback("filter specs success") {
            callback.onSuccess(specs)
        }

    private fun safeFilterSpecsError(callback: IFilterSpecsCallback, code: String, message: String): Boolean =
        runCallback("filter specs error $code") {
            callback.onError(code, message)
        }

    private fun safeStatusSuccess(callback: IBackendStatusCallback, status: BackendStatusDto): Boolean =
        runCallback("status success") {
            callback.onSuccess(status)
        }

    private fun safeStatusError(callback: IBackendStatusCallback, code: String, message: String): Boolean =
        runCallback("status error $code") {
            callback.onError(code, message)
        }

    private fun runCallback(label: String, send: () -> Unit): Boolean =
        try {
            send()
            true
        } catch (error: RemoteException) {
            Log.w(TAG, "Failed to send $label callback", error)
            false
        }

    private fun articleLogResult(result: String, callbackSent: Boolean): String =
        if (callbackSent) result else "$result callback=REMOTE_EXCEPTION"

    private fun validateCaller(callingUid: Int): Boolean =
        BackendRuntime.callerValidator.validate(callingUid)

    private fun ArticleFilterRequest.toCriteria(): FilterCriteria =
        FilterCriteria(
            titleQuery = titleQuery,
            ratingValues = ratingValues.toSet(),
            dynamicValues = dynamicValues.mapValues { (_, values) -> values.toSet() },
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

    private fun FilterSpec.toDto(): FilterSpecDto =
        FilterSpecDto(
            key = key,
            label = label,
            type = type.name,
            options = options,
        )

    private fun FilterCriteria.toLogString(): String =
        "title=${titleQuery?.takeIf { it.isNotBlank() } ?: "*"},ratings=${ratingValues.sorted().ifEmpty { listOf("*") }}"

    private companion object {
        const val TAG = "NewsBackendService"
        const val API_VERSION = 1
        const val SLOW_SCENARIO_DELAY_MS = 800L
    }
}
