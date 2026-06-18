package com.unitynews.news.data

import app.cash.turbine.test
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstNewsRepositoryTest {
    @Test
    fun `refresh success stores articles by criteria and observer emits them`() = runTest {
        val remote = FakeRemoteArticleDataSource(
            articleResult = Result.success(listOf(article(id = "1", title = "Unity", rating = 5))),
        )
        val local = InMemoryNewsLocalDataSource()
        val repository = OfflineFirstNewsRepository(local, remote)
        val criteria = FilterCriteria(titleQuery = "unity", ratingValues = setOf(5))

        val result = repository.refresh(criteria)

        assertTrue(result.isSuccess)
        repository.observeArticles(criteria).test {
            assertEquals(listOf("Unity"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh failure preserves cached articles`() = runTest {
        val criteria = FilterCriteria()
        val local = InMemoryNewsLocalDataSource()
        local.replace(criteria, listOf(article(id = "1", title = "Cached", rating = 3)))
        val remote = FakeRemoteArticleDataSource(
            articleResult = Result.failure(IllegalStateException("offline")),
        )
        val repository = OfflineFirstNewsRepository(local, remote)

        val result = repository.refresh(criteria)

        assertTrue(result.isFailure)
        repository.observeArticles(criteria).test {
            assertEquals(listOf("Cached"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `criteria specific cache does not mix different filter result sets`() = runTest {
        val local = InMemoryNewsLocalDataSource()
        val unityCriteria = FilterCriteria(titleQuery = "unity", ratingValues = setOf(5))
        val androidCriteria = FilterCriteria(titleQuery = "android", ratingValues = setOf(4))
        local.replace(unityCriteria, listOf(article(id = "1", title = "Unity", rating = 5)))
        local.replace(androidCriteria, listOf(article(id = "2", title = "Android", rating = 4)))
        val repository = OfflineFirstNewsRepository(
            local = local,
            remote = FakeRemoteArticleDataSource(articleResult = Result.success(emptyList())),
        )

        repository.observeArticles(unityCriteria).test {
            assertEquals(listOf("Unity"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
        repository.observeArticles(androidCriteria).test {
            assertEquals(listOf("Android"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mark stale does not delete cached articles`() = runTest {
        val criteria = FilterCriteria(titleQuery = "cached")
        val local = InMemoryNewsLocalDataSource()
        local.replace(criteria, listOf(article(id = "1", title = "Cached", rating = 2)))

        local.markStale(criteria, "backend unavailable")

        local.observe(criteria).test {
            assertEquals(listOf("Cached"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("backend unavailable", local.staleReason(criteria))
    }

    @Test
    fun `criteria with different custom filter values do not share cache entries`() = runTest {
        val local = InMemoryNewsLocalDataSource()
        val remote = FakeRemoteArticleDataSource(articleResult = Result.success(emptyList()))
        val repository = OfflineFirstNewsRepository(local, remote)
        val sportsCriteria = FilterCriteria(dynamicValues = mapOf("section" to setOf("sports")))
        val financeCriteria = FilterCriteria(dynamicValues = mapOf("section" to setOf("finance")))
        local.replace(sportsCriteria, listOf(article(id = "1", title = "Sports", rating = 4)))
        local.replace(financeCriteria, listOf(article(id = "2", title = "Finance", rating = 5)))

        repository.observeArticles(sportsCriteria).test {
            assertEquals(listOf("Sports"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
        repository.observeArticles(financeCriteria).test {
            assertEquals(listOf("Finance"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `criteria dynamic value ordering does not destabilize cache key`() = runTest {
        val local = InMemoryNewsLocalDataSource()
        val remote = FakeRemoteArticleDataSource(articleResult = Result.success(emptyList()))
        val repository = OfflineFirstNewsRepository(local, remote)
        val firstCriteria = FilterCriteria(
            dynamicValues = linkedMapOf(
                "topic" to linkedSetOf("unity", "android"),
                "region" to linkedSetOf("us", "eu"),
            ),
        )
        val reorderedCriteria = FilterCriteria(
            dynamicValues = linkedMapOf(
                "region" to linkedSetOf("eu", "us"),
                "topic" to linkedSetOf("android", "unity"),
            ),
        )
        local.replace(firstCriteria, listOf(article(id = "1", title = "Stable", rating = 4)))

        repository.observeArticles(reorderedCriteria).test {
            assertEquals(listOf("Stable"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh returns failure when local replace throws after remote success`() = runTest {
        val expected = IllegalStateException("disk write failed")
        val local = ThrowingNewsLocalDataSource(replaceFailure = expected)
        val remote = FakeRemoteArticleDataSource(
            articleResult = Result.success(listOf(article(id = "1", title = "Remote", rating = 4))),
        )
        val repository = OfflineFirstNewsRepository(local, remote)

        val result = repository.refresh(FilterCriteria())

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }

    @Test
    fun `refresh returns failure when local mark stale throws after remote failure`() = runTest {
        val expected = IllegalStateException("stale write failed")
        val local = ThrowingNewsLocalDataSource(markStaleFailure = expected)
        val remoteFailure = IllegalStateException("remote offline")
        val remote = FakeRemoteArticleDataSource(articleResult = Result.failure(remoteFailure))
        val repository = OfflineFirstNewsRepository(local, remote)

        val result = repository.refresh(FilterCriteria())

        assertTrue(result.isFailure)
        assertSame(expected, result.exceptionOrNull())
    }

    private fun article(
        id: String,
        title: String,
        rating: Int,
    ): Article = Article(
        id = id,
        title = title,
        description = "Description",
        imageUrl = "https://example.com/$id.png",
        rating = rating,
        placeholderRed = 1,
        placeholderGreen = 2,
        placeholderBlue = 3,
    )
}

private class FakeRemoteArticleDataSource(
    private val articleResult: Result<List<Article>>,
    private val filterSpecResult: Result<List<FilterSpec>> = Result.success(emptyList()),
) : RemoteArticleDataSource {
    override suspend fun getArticles(criteria: FilterCriteria): Result<List<Article>> = articleResult

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> = filterSpecResult
}

private class InMemoryNewsLocalDataSource : NewsLocalDataSource {
    private val cache = MutableStateFlow<Map<String, CacheEntry>>(emptyMap())

    override fun observe(criteria: FilterCriteria): Flow<List<Article>> {
        val key = criteria.cacheKey()
        return cache.map { entries -> entries[key]?.articles.orEmpty() }
    }

    override suspend fun replace(criteria: FilterCriteria, articles: List<Article>) {
        val key = criteria.cacheKey()
        cache.value = cache.value + (key to CacheEntry(articles = articles, staleReason = null))
    }

    override suspend fun markStale(criteria: FilterCriteria, reason: String) {
        val key = criteria.cacheKey()
        val entry = cache.value[key] ?: CacheEntry()
        cache.value = cache.value + (key to entry.copy(staleReason = reason))
    }

    fun staleReason(criteria: FilterCriteria): String? = cache.value[criteria.cacheKey()]?.staleReason

    private fun FilterCriteria.cacheKey(): String {
        val title = titleQuery?.trim().orEmpty()
        val ratings = ratingValues.sorted().joinToString(separator = ",")
        val dynamic = dynamicValues.toSortedMap().entries.joinToString(separator = "|") { (key, values) ->
            "$key=${values.sorted().joinToString(separator = ",")}"
        }
        return "title=$title|ratings=$ratings|dynamic=$dynamic"
    }

    private data class CacheEntry(
        val articles: List<Article> = emptyList(),
        val staleReason: String? = null,
    )
}

private class ThrowingNewsLocalDataSource(
    private val replaceFailure: Throwable? = null,
    private val markStaleFailure: Throwable? = null,
) : NewsLocalDataSource {
    override fun observe(criteria: FilterCriteria): Flow<List<Article>> =
        MutableStateFlow(emptyList())

    override suspend fun replace(criteria: FilterCriteria, articles: List<Article>) {
        replaceFailure?.let { throw it }
    }

    override suspend fun markStale(criteria: FilterCriteria, reason: String) {
        markStaleFailure?.let { throw it }
    }
}
