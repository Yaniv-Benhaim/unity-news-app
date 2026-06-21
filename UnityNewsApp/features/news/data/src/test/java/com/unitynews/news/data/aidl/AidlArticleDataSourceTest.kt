package com.unitynews.news.data.aidl

import android.os.RemoteException
import com.unitynews.contract.ArticleDto
import com.unitynews.contract.ArticleFilterRequest
import com.unitynews.contract.BackendStatusDto
import com.unitynews.contract.FilterSpecDto
import com.unitynews.contract.IArticlesCallback
import com.unitynews.contract.IBackendStatusCallback
import com.unitynews.contract.IFilterSpecsCallback
import com.unitynews.contract.INewsBackendService
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.model.FilterType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AidlArticleDataSourceTest {
    @Test
    fun `getArticles sends criteria over aidl and maps returned articles`() = runTest {
        val backend = FakeBackendService(
            articleHandler = { request, callback ->
                assertEquals(
                    mapOf(
                        "title" to listOf("unity"),
                        "rating" to listOf("5", "3"),
                        "section" to listOf("sports", "finance"),
                        "region" to listOf("emea"),
                    ),
                    request.filterValues,
                )
                assertTrue(request.requestId.isNotBlank())
                callback.onSuccess(listOf(articleDto(id = "1", title = "Unity News")))
            },
        )
        val dataSource = AidlArticleDataSource(backend = { backend })

        val result = dataSource.getArticles(
            FilterCriteria(
                filterValues = linkedMapOf(
                    "title" to linkedSetOf("unity"),
                    "rating" to linkedSetOf("5", "3"),
                    "section" to linkedSetOf("sports", "finance"),
                    "region" to linkedSetOf("emea"),
                ),
            ),
        )

        assertEquals(
            Result.success(listOf(article(id = "1", title = "Unity News"))),
            result,
        )
    }

    @Test
    fun `getArticles fails when backend api version is unsupported`() = runTest {
        val dataSource = AidlArticleDataSource(
            backend = { FakeBackendService(apiVersion = 1) },
        )

        val result = dataSource.getArticles(FilterCriteria())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `getArticles returns callback error as failure`() = runTest {
        val dataSource = AidlArticleDataSource(
            backend = {
                FakeBackendService(
                    articleHandler = { _, callback ->
                        callback.onError("OFFLINE", "Backend offline")
                    },
                )
            },
        )

        val result = dataSource.getArticles(FilterCriteria())

        assertTrue(result.isFailure)
        assertEquals("OFFLINE: Backend offline", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getArticles returns RemoteException thrown by service call as failure`() = runTest {
        val expected = RemoteException("binder died")
        val dataSource = AidlArticleDataSource(
            backend = {
                FakeBackendService(
                    articleHandler = { _, _ -> throw expected },
                )
            },
        )

        val result = dataSource.getArticles(FilterCriteria())

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }

    @Test
    fun `getArticles ignores duplicate callback completion`() = runTest {
        val dataSource = AidlArticleDataSource(
            backend = {
                FakeBackendService(
                    articleHandler = { _, callback ->
                        callback.onSuccess(listOf(articleDto(id = "first", title = "First")))
                        callback.onError("LATE", "late duplicate")
                    },
                )
            },
        )

        val result = dataSource.getArticles(FilterCriteria())

        assertEquals(
            Result.success(listOf(article(id = "first", title = "First"))),
            result,
        )
    }

    @Test
    fun `getFilterSpecs calls backend and maps type strings including unknown values`() = runTest {
        val dataSource = AidlArticleDataSource(
            backend = {
                FakeBackendService(
                    filterSpecHandler = { callback ->
                        callback.onSuccess(
                            listOf(
                                FilterSpecDto(
                                    key = "title",
                                    label = "Title",
                                    type = "text",
                                    options = emptyList(),
                                ),
                                FilterSpecDto(
                                    key = "section",
                                    label = "Section",
                                    type = "multi_select",
                                    options = listOf("Sports", "Finance"),
                                ),
                                FilterSpecDto(
                                    key = "owner",
                                    label = "Owner",
                                    type = "user_picker",
                                    options = emptyList(),
                                ),
                            ),
                        )
                    },
                )
            },
        )

        val result = dataSource.getFilterSpecs()

        assertEquals(
            Result.success(
                listOf(
                    FilterSpec("title", "Title", FilterType.Text),
                    FilterSpec("section", "Section", FilterType.MultiSelect, listOf("Sports", "Finance")),
                    FilterSpec("owner", "Owner", FilterType.Unsupported),
                ),
            ),
            result,
        )
    }

    @Test
    fun `getFilterSpecs returns callback error as failure`() = runTest {
        val dataSource = AidlArticleDataSource(
            backend = {
                FakeBackendService(
                    filterSpecHandler = { callback ->
                        callback.onError("NO_SPECS", "No specs available")
                    },
                )
            },
        )

        val result = dataSource.getFilterSpecs()

        assertTrue(result.isFailure)
        assertEquals("NO_SPECS: No specs available", result.exceptionOrNull()?.message)
    }

    private fun articleDto(
        id: String,
        title: String,
    ): ArticleDto = ArticleDto(
        id = id,
        title = title,
        description = "Description",
        imageUrl = "https://example.com/$id.png",
        rating = 4,
        placeholderRed = 1,
        placeholderGreen = 2,
        placeholderBlue = 3,
    )

    private fun article(
        id: String,
        title: String,
    ): Article = Article(
        id = id,
        title = title,
        description = "Description",
        imageUrl = "https://example.com/$id.png",
        rating = 4,
        placeholderRed = 1,
        placeholderGreen = 2,
        placeholderBlue = 3,
    )
}

private class FakeBackendService(
    private val apiVersion: Int = 2,
    private val articleHandler: (ArticleFilterRequest, IArticlesCallback) -> Unit = { _, callback ->
        callback.onSuccess(emptyList())
    },
    private val filterSpecHandler: (IFilterSpecsCallback) -> Unit = { callback ->
        callback.onSuccess(emptyList())
    },
) : INewsBackendService.Stub() {
    override fun getApiVersion(): Int = apiVersion

    override fun getFilterSpecs(callback: IFilterSpecsCallback) {
        filterSpecHandler(callback)
    }

    override fun getArticles(request: ArticleFilterRequest, callback: IArticlesCallback) {
        articleHandler(request, callback)
    }

    override fun getBackendStatus(callback: IBackendStatusCallback) {
        callback.onSuccess(BackendStatusDto(isRunning = true, scenario = "test", articleCount = 0))
    }
}
