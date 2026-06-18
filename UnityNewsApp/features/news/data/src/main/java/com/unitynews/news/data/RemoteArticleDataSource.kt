package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec

interface RemoteArticleDataSource {
    suspend fun getArticles(criteria: FilterCriteria): Result<List<Article>>

    suspend fun getFilterSpecs(): Result<List<FilterSpec>>
}
