package com.unitynews.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for article cache operations.
 *
 * The public methods describe feature-level operations. The protected methods
 * below are lower-level SQL steps used inside transactions.
 */
@Dao
abstract class NewsDao {
    /**
     * Replace one cached query atomically.
     *
     * Both the article rows and the query mapping are written in one transaction
     * so the UI never sees a half-updated cache.
     */
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

    /**
     * Mark a cached query as stale after a failed refresh.
     *
     * If the query was never cached before, we create an empty stale entry so
     * the UI can still show a meaningful backend error.
     */
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

    /** Observe the metadata for one filter selection. */
    @Query("SELECT * FROM cached_queries WHERE criteriaHash = :criteriaHash")
    abstract fun observeCachedQuery(criteriaHash: String): Flow<CachedQueryEntity?>

    /** Observe article rows by ID. Ordering is restored in RoomNewsLocalDataSource. */
    @Query("SELECT * FROM articles WHERE id IN (:articleIds)")
    abstract fun observeArticlesByIds(articleIds: List<String>): Flow<List<ArticleEntity>>
}
