package com.unitynews.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NewsDao {
    @Transaction
    open suspend fun replaceCachedQuery(
        criteriaHash: String,
        articles: List<ArticleEntity>,
        refreshedAt: Long,
    ) {
        upsertArticles(articles)
        upsertCachedQuery(
            CachedQueryEntity(
                criteriaHash = criteriaHash,
                articleIds = encodeArticleIds(articles.map { it.id }),
                lastSuccessfulRefreshAt = refreshedAt,
                staleReason = null,
            ),
        )
    }

    @Transaction
    open suspend fun markStale(criteriaHash: String, reason: String) {
        val updatedRows = updateStaleReason(criteriaHash, reason)
        if (updatedRows == 0) {
            insertCachedQueryIfMissing(
                CachedQueryEntity(
                    criteriaHash = criteriaHash,
                    articleIds = encodeArticleIds(emptyList()),
                    lastSuccessfulRefreshAt = 0L,
                    staleReason = reason,
                ),
            )
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertArticles(articles: List<ArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun upsertCachedQuery(cachedQuery: CachedQueryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertCachedQueryIfMissing(cachedQuery: CachedQueryEntity)

    @Query(
        """
        UPDATE cached_queries
        SET staleReason = :reason
        WHERE criteriaHash = :criteriaHash
        """,
    )
    protected abstract suspend fun updateStaleReason(criteriaHash: String, reason: String): Int

    @Query("SELECT * FROM cached_queries WHERE criteriaHash = :criteriaHash")
    abstract fun observeCachedQuery(criteriaHash: String): Flow<CachedQueryEntity?>

    @Query("SELECT * FROM articles WHERE id IN (:articleIds)")
    abstract suspend fun getArticlesByIds(articleIds: List<String>): List<ArticleEntity>
}
