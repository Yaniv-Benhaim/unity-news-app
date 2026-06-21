package com.example.unitynewsbackend.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import com.example.unitynewsbackend.BackendRuntime
import com.example.unitynewsbackend.R
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Bound AIDL service consumed by the reader app.
 *
 * This class is the backend's API surface. It validates callers, applies the
 * selected demo scenario, filters articles, and returns Parcelable DTOs through
 * callbacks.
 */
class NewsBackendService : Service() {
    /** IO scope keeps binder callbacks responsive while work runs off the main thread. */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val binder = object : INewsBackendService.Stub() {
        /** Contract version checked by the reader before making requests. */
        override fun getApiVersion(): Int = API_VERSION

        /** Return backend-driven filter definitions to the reader app. */
        override fun getFilterSpecs(callback: IFilterSpecsCallback) {
            val callingUid = Binder.getCallingUid()
            serviceScope.launch {
                if (!validateCaller(callingUid)) {
                    safeFilterSpecsError(
                        callback,
                        ERROR_CODE_UNAUTHORIZED,
                        getString(R.string.backend_error_caller_not_authorized),
                    )
                    return@launch
                }

                resultPreservingCancellation {
                    val articles = BackendRuntime.repository.getArticles()
                    BackendRuntime.getFilterSpecsUseCase(articles).map { it.toDto() }
                }.onSuccess { specs ->
                    safeFilterSpecsSuccess(callback, specs)
                }
                    .onFailure { error ->
                        safeFilterSpecsError(
                            callback,
                            ERROR_CODE_SERVER_ERROR,
                            error.message ?: getString(R.string.backend_error_unable_to_load_filters),
                        )
                    }
            }
        }

        /** Return articles for a filter request, applying the currently selected scenario. */
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
                        ERROR_CODE_UNAUTHORIZED,
                        getString(R.string.backend_error_caller_not_authorized),
                    )
                    logArticlesRequest(
                        requestLabel,
                        criteria,
                        scenario,
                        articleLogResult(ERROR_CODE_UNAUTHORIZED, callbackSent),
                        startedAt,
                    )
                    return@launch
                }

                when (scenario) {
                    // Demo scenarios make error, empty, and slow states easy to review.
                    ServerScenario.Unauthorized -> {
                        val callbackSent = safeArticleError(
                            callback,
                            ERROR_CODE_UNAUTHORIZED,
                            getString(R.string.backend_error_caller_not_authorized),
                        )
                        logArticlesRequest(
                            requestLabel,
                            criteria,
                            scenario,
                            articleLogResult(ERROR_CODE_UNAUTHORIZED, callbackSent),
                            startedAt,
                        )
                    }

                    ServerScenario.ServerError -> {
                        val callbackSent = safeArticleError(
                            callback,
                            ERROR_CODE_SERVER_ERROR,
                            getString(R.string.backend_error_scenario_forced_server_error),
                        )
                        logArticlesRequest(
                            requestLabel,
                            criteria,
                            scenario,
                            articleLogResult(ERROR_CODE_SERVER_ERROR, callbackSent),
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

        /** Return service health/status for admin-style clients. */
        override fun getBackendStatus(callback: IBackendStatusCallback) {
            val callingUid = Binder.getCallingUid()
            serviceScope.launch {
                if (!validateCaller(callingUid)) {
                    safeStatusError(
                        callback,
                        ERROR_CODE_UNAUTHORIZED,
                        getString(R.string.backend_error_caller_not_authorized),
                    )
                    return@launch
                }

                resultPreservingCancellation {
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
                            ERROR_CODE_SERVER_ERROR,
                            error.message ?: getString(R.string.backend_error_unable_to_load_status),
                        )
                    }
            }
        }
    }

    /** Android gives clients this binder when they bind to the service. */
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
        // Repository returns all articles; domain use case applies request criteria.
        resultPreservingCancellation {
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
                ERROR_CODE_SERVER_ERROR,
                error.message ?: getString(R.string.backend_error_unable_to_load_articles),
            )
            logArticlesRequest(
                requestLabel,
                criteria,
                scenario,
                articleLogResult(ERROR_CODE_SERVER_ERROR, callbackSent),
                startedAt,
            )
        }
    }

    /** Write a compact request entry visible in the backend console. */
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

    /** Callback helpers prevent a dead remote process from crashing the service. */
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

    /** Convert exceptions to Result while preserving coroutine cancellation. */
    private suspend fun <T> resultPreservingCancellation(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }

    /** Add callback delivery state to request logs for easier debugging. */
    private fun articleLogResult(result: String, callbackSent: Boolean): String =
        if (callbackSent) result else "$result callback=REMOTE_EXCEPTION"

    /** Trust same-process calls and signed reader-app calls only. */
    private fun validateCaller(callingUid: Int): Boolean =
        BackendRuntime.callerValidator.validate(callingUid)

    /** Convert the AIDL request DTO into the backend domain filter model. */
    private fun ArticleFilterRequest.toCriteria(): FilterCriteria =
        FilterCriteria(
            filterValues = filterValues.mapValues { (_, values) -> values.toSet() },
        )

    /** Convert a backend domain article to the shared AIDL DTO. */
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

    /** Convert backend filter metadata to the shared AIDL DTO. */
    private fun FilterSpec.toDto(): FilterSpecDto =
        FilterSpecDto(
            key = key,
            label = label,
            type = type.name,
            options = options,
        )

    /** Keep logs short but still useful for debugging request behavior. */
    private fun FilterCriteria.toLogString(): String =
        filterValues
            .toSortedMap()
            .takeIf { it.isNotEmpty() }
            ?.entries
            ?.joinToString(separator = ";") { (key, values) ->
                "$key=${values.sorted().ifEmpty { listOf("*") }}"
            }
            ?: "*"

    private companion object {
        const val TAG = "NewsBackendService"
        const val API_VERSION = 2
        private const val ERROR_CODE_SERVER_ERROR = "SERVER_ERROR"
        private const val ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED"
        const val SLOW_SCENARIO_DELAY_MS = 800L
    }
}
