package com.unitynews.server.data

import android.content.Context
import com.unitynews.server.domain.model.Article
import com.unitynews.server.domain.repository.ArticleRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AssetArticleRepository(
    context: Context,
    private val assetName: String = "articles.json",
) : ArticleRepository {
    private val applicationContext = context.applicationContext
    private val mutex = Mutex()
    private var cachedArticles: List<Article>? = null

    override suspend fun getArticles(): List<Article> = mutex.withLock {
        cachedArticles ?: readArticles().also { cachedArticles = it }
    }

    private fun readArticles(): List<Article> =
        applicationContext.assets.open(assetName).bufferedReader().use { reader ->
            ArticleJsonParser.parse(reader.readText())
        }
}
