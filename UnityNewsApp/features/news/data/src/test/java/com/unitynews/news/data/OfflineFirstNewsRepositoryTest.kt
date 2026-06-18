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
        return "title=$title|ratings=$ratings"
    }

    private data class CacheEntry(
        val articles: List<Article> = emptyList(),
        val staleReason: String? = null,
    )
}
