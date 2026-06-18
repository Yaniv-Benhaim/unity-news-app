package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.flow.Flow

interface NewsLocalDataSource {
    fun observe(criteria: FilterCriteria): Flow<List<Article>>

    suspend fun replace(criteria: FilterCriteria, articles: List<Article>)

    suspend fun markStale(criteria: FilterCriteria, reason: String)
}
