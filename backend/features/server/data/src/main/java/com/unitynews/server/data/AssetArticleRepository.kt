package com.unitynews.server.data

import android.content.Context
import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.repository.ArticleRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * ArticleRepository backed by the app's bundled `articles.json` asset.
 *
 * The file is parsed lazily once and cached in memory, which is enough for this
 * local backend and keeps repeated AIDL calls fast.
 */
class AssetArticleRepository(
    context: Context,
    private val assetName: String = "articles.json",
) : ArticleRepository {
    private val applicationContext = context.applicationContext
    private val mutex = Mutex()
    private var cachedArticles: List<Article>? = null

    /** Load once, then serve the cached immutable list. */
    override suspend fun getArticles(): List<Article> = mutex.withLock {
        cachedArticles ?: readArticles().also { cachedArticles = it }
    }

    /** Read and parse the bundled asset file. */
    private fun readArticles(): List<Article> =
        applicationContext.assets.open(assetName).bufferedReader().use { reader ->
            ArticleJsonParser.parse(reader.readText())
        }
}
