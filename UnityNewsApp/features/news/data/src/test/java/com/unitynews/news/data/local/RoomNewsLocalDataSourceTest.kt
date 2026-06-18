package com.unitynews.news.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RoomNewsLocalDataSourceTest {
    private lateinit var database: NewsDatabase
    private lateinit var local: RoomNewsLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        local = RoomNewsLocalDataSource(database.newsDao(), clock = { 100L })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replace stores JSON ids transactionally and preserves cached article order`() = runTest {
        val criteria = FilterCriteria(titleQuery = "ordered")
        local.replace(
            criteria,
            listOf(
                article(id = "2", title = "Second"),
                article(id = "1", title = "First"),
            ),
        )

        local.observe(criteria).test {
            assertEquals(listOf("Second", "First"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `active observer sees shared article updates from another criteria refresh`() = runTest {
        val criteriaA = FilterCriteria(titleQuery = "a")
        val criteriaB = FilterCriteria(titleQuery = "b")
        local.replace(criteriaA, listOf(article(id = "1", title = "Old shared")))

        local.observe(criteriaA).test {
            assertEquals(listOf("Old shared"), awaitItem().map { it.title })

            local.replace(criteriaB, listOf(article(id = "1", title = "Updated shared")))

            assertEquals(listOf("Updated shared"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `active observer sees article table updates when cached query ids are unchanged`() = runTest {
        val criteria = FilterCriteria(titleQuery = "same ids")
        local.replace(criteria, listOf(article(id = "1", title = "Old title")))

        local.observe(criteria).test {
            assertEquals(listOf("Old title"), awaitItem().map { it.title })

            database.openHelper.writableDatabase.execSQL(
                "UPDATE articles SET title = ? WHERE id = ?",
                arrayOf("Updated title", "1"),
            )
            database.invalidationTracker.refreshAsync()

            assertEquals(listOf("Updated title"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `criteria cache isolation includes custom dynamic filters`() = runTest {
        val sportsCriteria = FilterCriteria(dynamicValues = mapOf("section" to setOf("sports")))
        val financeCriteria = FilterCriteria(dynamicValues = mapOf("section" to setOf("finance")))
        local.replace(sportsCriteria, listOf(article(id = "1", title = "Sports")))
        local.replace(financeCriteria, listOf(article(id = "2", title = "Finance")))

        local.observe(sportsCriteria).test {
            assertEquals(listOf("Sports"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
        local.observe(financeCriteria).test {
            assertEquals(listOf("Finance"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mark stale preserves cached query rows and article rows`() = runTest {
        val criteria = FilterCriteria(titleQuery = "cached")
        local.replace(criteria, listOf(article(id = "1", title = "Cached")))

        local.markStale(criteria, "offline")

        local.observe(criteria).test {
            assertEquals(listOf("Cached"), awaitItem().map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun article(
        id: String,
        title: String,
    ): Article = Article(
        id = id,
        title = title,
        description = "Description",
        imageUrl = "https://example.com/$id.png",
        rating = 5,
        placeholderRed = 1,
        placeholderGreen = 2,
        placeholderBlue = 3,
    )
}
