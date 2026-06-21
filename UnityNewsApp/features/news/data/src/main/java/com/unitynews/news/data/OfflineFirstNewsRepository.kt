package com.unitynews.news.data

import com.unitynews.news.domain.model.Article
import com.unitynews.news.domain.model.FilterCriteria
import com.unitynews.news.domain.model.FilterSpec
import com.unitynews.news.domain.repository.NewsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first implementation of NewsRepository.
 *
 * Reads always come from the local database, so the screen can show cached
 * content instantly. Refreshes go to the backend and then update the cache.
 */
class OfflineFirstNewsRepository(
    private val local: NewsLocalDataSource,
    private val remote: RemoteArticleDataSource,
) : NewsRepository {
    /** The UI observes Room. It does not wait for the backend before rendering. */
    override fun observeArticles(criteria: FilterCriteria): Flow<List<Article>> =
        local.observe(criteria)

    /**
     * Refresh remote data for a filter and persist it locally.
     *
     * If the backend fails, we mark the cached query as stale. That lets the UI
     * keep showing old content while also explaining that the newest refresh
     * failed.
     */
    override suspend fun refresh(criteria: FilterCriteria): Result<Unit> =
        remote.getArticles(criteria).fold(
            onSuccess = { articles ->
                resultPreservingCancellation { local.replace(criteria, articles) }
            },
            onFailure = { error ->
                if (error is CancellationException) {
                    throw error
                }
                resultPreservingCancellation {
                    local.markStale(criteria, error.message ?: "Remote refresh failed")
                }
                    .fold(
                        onSuccess = { Result.failure(error) },
                        onFailure = { localError -> Result.failure(localError) },
                    )
            },
        )

    /** Filter specs are backend-owned because they can change without an app release. */
    override suspend fun getFilterSpecs(): Result<List<FilterSpec>> =
        remote.getFilterSpecs()

    /** Cancellation must escape so coroutine cancellation still works correctly. */
    private suspend fun resultPreservingCancellation(block: suspend () -> Unit): Result<Unit> =
        try {
            block()
            Result.success(Unit)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
}
