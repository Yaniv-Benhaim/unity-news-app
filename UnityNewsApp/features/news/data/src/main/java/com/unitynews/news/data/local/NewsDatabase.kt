package com.unitynews.news.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the reader cache.
 *
 * It keeps articles separately from cached queries, which lets multiple filter
 * selections reuse the same article rows.
 */
@Database(
    entities = [ArticleEntity::class, CachedQueryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}
