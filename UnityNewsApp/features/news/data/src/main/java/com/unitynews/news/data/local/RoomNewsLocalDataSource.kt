package com.unitynews.news.data.local

import com.unitynews.news.data.NewsLocalDataSource
import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Room-backed implementation of the local cache.
 *
 * The cache is query-based: each filter selection points to an ordered list of
 * article IDs. The actual article rows live in a shared articles table.
 */
class RoomNewsLocalDataSource(
    private val dao: NewsDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : NewsLocalDataSource {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observe(criteria: FilterCriteria): Flow<List<Article>> {
        // First observe the cached query, then observe the article rows it points to.
        return dao.observeCachedQuery(criteria.toCriteriaHash())
            .map { cachedQuery -> cachedQuery?.decodeArticleIds().orEmpty() }
            .flatMapLatest { articleIds ->
                if (articleIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    dao.observeArticlesByIds(articleIds).map { entities ->
                        val entitiesById = entities.associateBy { it.id }
                        // Room does not guarantee the same order as the ID list, so rebuild it.
                        articleIds.mapNotNull { id -> entitiesById[id]?.toDomain() }
                    }
                }
            }
    }

    /** Persist the latest successful backend response for this filter selection. */
    override suspend fun replace(criteria: FilterCriteria, articles: List<Article>) {
        val fetchedAt = clock()
        dao.replaceCachedQuery(
            criteriaHash = criteria.toCriteriaHash(),
            articles = articles.map { it.toEntity(lastFetchedAt = fetchedAt) },
            refreshedAt = fetchedAt,
        )
    }

    /** Keep old cached data but record that the newest refresh failed. */
    override suspend fun markStale(criteria: FilterCriteria, reason: String) {
        dao.markStale(criteriaHash = criteria.toCriteriaHash(), reason = reason)
    }
}
