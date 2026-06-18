package com.unitynews.server.domain.repository

import com.unitynews.server.domain.model.Article

interface ArticleRepository {
    suspend fun getArticles(): List<Article>
}
