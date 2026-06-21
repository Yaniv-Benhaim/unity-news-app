package com.unitynews.server.domain.repository

import com.unitynews.server.domain.model.Article

/**
 * Domain contract for backend article storage.
 *
 * Today the data module reads from app assets. A production backend could use a
 * database, network source, or generated feed while keeping use cases unchanged.
 */
interface ArticleRepository {
    suspend fun getArticles(): List<Article>
}
