package com.unitynews.news.presentation

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.model.FilterType
import com.unitynews.news.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `cached articles produce content with those articles`() = runTest {
        val cachedArticles = listOf(article(id = "1", title = "Unity"))
        val repository = FakeNewsRepository(
            articles = MutableStateFlow(cachedArticles),
            filterSpecsResult = Result.success(
                listOf(FilterSpec(key = "title", label = "Title", type = FilterType.Text)),
            ),
        )

        val viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is NewsUiState.Content)
        state as NewsUiState.Content
        assertEquals(cachedArticles, state.articles)
        assertEquals(listOf("Title"), state.filters.map { it.label })
        assertEquals(false, state.isRefreshing)
        assertNull(state.staleMessage)
    }

    @Test
    fun `known title and rating filter keys update built in criteria fields`() = runTest {
        val repository = FakeNewsRepository(
            filterSpecsResult = Result.success(
                listOf(
                    FilterSpec(key = "title", label = "Title", type = FilterType.Text),
                    FilterSpec(key = "rating", label = "Rating", type = FilterType.MultiSelect),
                ),
            ),
        )
        val viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        viewModel.updateTextFilter(key = "title", value = "unity")
        viewModel.toggleMultiSelectFilter(key = "rating", option = "5", selected = true)
        advanceUntilIdle()

        assertEquals("unity", repository.observedCriteria.last().titleQuery)
        assertEquals(setOf(5), repository.observedCriteria.last().ratingValues)
        assertEquals(emptyMap<String, Set<String>>(), repository.observedCriteria.last().dynamicValues)
    }

    @Test
    fun `unknown dynamic filter keys update dynamic criteria values`() = runTest {
        val repository = FakeNewsRepository(
            filterSpecsResult = Result.success(
                listOf(
                    FilterSpec(key = "section", label = "Section", type = FilterType.MultiSelect),
                    FilterSpec(key = "source", label = "Source", type = FilterType.Text),
                ),
            ),
        )
        val viewModel = NewsViewModel(repository)
        advanceUntilIdle()

        viewModel.toggleMultiSelectFilter(key = "section", option = "Tech", selected = true)
        viewModel.updateTextFilter(key = "source", value = "Wire")
        advanceUntilIdle()

        assertEquals(
            mapOf(
                "section" to setOf("Tech"),
                "source" to setOf("Wire"),
            ),
            repository.observedCriteria.last().dynamicValues,
        )
    }

    private fun article(
        id: String,
        title: String,
    ): Article = Article(
        id = id,
        title = title,
        description = "Description",
        imageUrl = "https://example.com/$id.png",
        rating = 4,
        placeholderRed = 12,
        placeholderGreen = 34,
        placeholderBlue = 56,
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeNewsRepository(
    private val articles: MutableStateFlow<List<Article>> = MutableStateFlow(emptyList()),
    private val filterSpecsResult: Result<List<FilterSpec>> = Result.success(emptyList()),
    private val refreshResult: Result<Unit> = Result.success(Unit),
) : NewsRepository {
    val observedCriteria = mutableListOf<FilterCriteria>()

    override fun observeArticles(criteria: FilterCriteria): Flow<List<Article>> {
        observedCriteria += criteria
        return articles
    }

    override suspend fun refresh(criteria: FilterCriteria): Result<Unit> = refreshResult

    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> = filterSpecsResult
}
