package com.unitynews.news.data.aidl

import android.os.RemoteException
import com.unitynews.contract.ArticleDto
import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.FilterSpecDto
import com.unitynews.contract.IArticlesCallback
import com.unitynews.contract.IFilterSpecsCallback
import com.unitynews.contract.INewsBackendService
import com.unitynews.news.data.RemoteArticleDataSource
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.model.FilterType
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AidlArticleDataSource(
    private val backend: suspend () -> INewsBackendService,
) : RemoteArticleDataSource {
    override suspend fun getArticles(criteria: FilterCriteria): Result<List<Article>> =
        resultPreservingCancellation {
            val service = backend()
            service.requireSupportedApiVersion()
            service.awaitArticles(criteria.toRequest())
        }

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> =
        resultPreservingCancellation {
            val service = backend()
            service.requireSupportedApiVersion()
            service.awaitFilterSpecs()
        }

    private suspend fun INewsBackendService.awaitArticles(
        request: ArticleFilterRequest,
    ): Result<List<Article>> =
        suspendCancellableCoroutine { continuation ->
            val completed = AtomicBoolean(false)

            fun complete(result: Result<List<Article>>) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    continuation.resume(result)
                }
            }

            val callback = object : IArticlesCallback.Stub() {
                override fun onSuccess(articles: List<ArticleDto>?) {
                    complete(runCatching { articles.orEmpty().map { it.toArticle() } })
                }

                override fun onError(code: String?, message: String?) {
                    complete(Result.failure(AidlBackendException(code, message)))
                }
            }

            continuation.invokeOnCancellation {
                completed.set(true)
            }

            try {
                getArticles(request, callback)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                complete(Result.failure(error))
            }
        }

    private suspend fun INewsBackendService.awaitFilterSpecs(): Result<List<FilterSpec>> =
        suspendCancellableCoroutine { continuation ->
            val completed = AtomicBoolean(false)

            fun complete(result: Result<List<FilterSpec>>) {
                if (completed.compareAndSet(false, true) && continuation.isActive) {
                    continuation.resume(result)
                }
            }

            val callback = object : IFilterSpecsCallback.Stub() {
                override fun onSuccess(specs: List<FilterSpecDto>?) {
                    complete(runCatching { specs.orEmpty().map { it.toFilterSpec() } })
                }

                override fun onError(code: String?, message: String?) {
                    complete(Result.failure(AidlBackendException(code, message)))
                }
            }

            continuation.invokeOnCancellation {
                completed.set(true)
            }

            try {
                getFilterSpecs(callback)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                complete(Result.failure(error))
            }
        }

    private fun INewsBackendService.requireSupportedApiVersion() {
        val backendApiVersion = apiVersion
        if (backendApiVersion != SUPPORTED_API_VERSION) {
            throw IllegalStateException("Unsupported backend API version $backendApiVersion")
        }
    }

    private suspend fun <T> resultPreservingCancellation(block: suspend () -> Result<T>): Result<T> =
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }

    private companion object {
        const val SUPPORTED_API_VERSION = 2
    }
}

internal fun FilterCriteria.toRequest(): ArticleFilterRequest =
    ArticleFilterRequest(
        titleQuery = titleQuery,
        ratingValues = ratingValues.toList(),
        requestId = UUID.randomUUID().toString(),
        dynamicValues = dynamicValues.mapValues { (_, values) -> values.toList() },
    )

private fun ArticleDto.toArticle(): Article =
    Article(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        rating = rating,
        placeholderRed = placeholderRed,
        placeholderGreen = placeholderGreen,
        placeholderBlue = placeholderBlue,
    )

private fun FilterSpecDto.toFilterSpec(): FilterSpec =
    FilterSpec(
        key = key,
        label = label,
        type = type.toFilterType(),
        options = options,
    )

private fun String.toFilterType(): FilterType =
    when (trim().replace("-", "_").lowercase()) {
        "text" -> FilterType.Text
        "multiselect", "multi_select" -> FilterType.MultiSelect
        else -> FilterType.Unsupported
    }

private class AidlBackendException(
    code: String?,
    message: String?,
) : RemoteException("${code.orEmpty()}: ${message.orEmpty()}")
