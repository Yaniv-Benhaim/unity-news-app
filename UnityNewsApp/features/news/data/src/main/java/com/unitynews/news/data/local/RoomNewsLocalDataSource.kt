package com.unitynews.news.data.local

import com.unitynews.news.data.NewsLocalDataSource
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNewsLocalDataSource(
    private val dao: NewsDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : NewsLocalDataSource {
    override fun observe(criteria: FilterCriteria): Flow<List<Article>> {
        return dao.observeCachedQuery(criteria.toCriteriaHash()).map { cachedQuery ->
            val articleIds = cachedQuery?.decodeArticleIds().orEmpty()
            if (articleIds.isEmpty()) {
                emptyList()
            } else {
                val entitiesById = dao.getArticlesByIds(articleIds).associateBy { it.id }
                articleIds.mapNotNull { id -> entitiesById[id]?.toDomain() }
            }
        }
    }

    override suspend fun replace(criteria: FilterCriteria, articles: List<Article>) {
        val fetchedAt = clock()
        dao.replaceCachedQuery(
            criteriaHash = criteria.toCriteriaHash(),
            articles = articles.map { it.toEntity(lastFetchedAt = fetchedAt) },
            refreshedAt = fetchedAt,
        )
    }

    override suspend fun markStale(criteria: FilterCriteria, reason: String) {
        dao.markStale(criteriaHash = criteria.toCriteriaHash(), reason = reason)
    }
}
