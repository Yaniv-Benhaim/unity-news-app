package com.unitynews.news.domain.repository

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun observeArticles(criteria: FilterCriteria): Flow<List<Article>>

    suspend fun refresh(criteria: FilterCriteria): Result<Unit>

    suspend fun getFilterSpecs(): Result<List<FilterSpec>>
}
