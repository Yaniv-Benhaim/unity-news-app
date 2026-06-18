package com.example.unitynewsapp.di

import android.content.Context
import androidx.room.Room
import com.unitynews.news.data.NewsLocalDataSource
import com.unitynews.news.data.OfflineFirstNewsRepository
import com.unitynews.news.data.RemoteArticleDataSource
import com.unitynews.news.data.aidl.AidlArticleDataSource
import com.unitynews.news.data.aidl.AndroidBackendConnection
import com.unitynews.news.data.aidl.AndroidPackageInspector
import com.unitynews.news.data.aidl.BackendAvailabilityChecker
import com.unitynews.news.data.aidl.BackendConnection
import com.unitynews.news.data.aidl.PackageInspector
import com.unitynews.news.data.local.NewsDatabase
import com.unitynews.news.data.local.RoomNewsLocalDataSource
import com.unitynews.news.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NewsAppModule {
    @Provides
    @Singleton
    fun provideNewsDatabase(
        @ApplicationContext context: Context,
    ): NewsDatabase =
        Room.databaseBuilder(
            context,
            NewsDatabase::class.java,
            "unity-news.db",
        ).build()

    @Provides
    fun provideNewsLocalDataSource(database: NewsDatabase): NewsLocalDataSource =
        RoomNewsLocalDataSource(database.newsDao())

    @Provides
    @Singleton
    fun provideBackendConnection(
        @ApplicationContext context: Context,
    ): BackendConnection =
        AndroidBackendConnection(context)

    @Provides
    fun provideRemoteArticleDataSource(
        backendConnection: BackendConnection,
    ): RemoteArticleDataSource =
        AidlArticleDataSource(
            backend = backendConnection::connect,
        )

    @Provides
    fun provideNewsRepository(
        localDataSource: NewsLocalDataSource,
        remoteArticleDataSource: RemoteArticleDataSource,
    ): NewsRepository =
        OfflineFirstNewsRepository(
            local = localDataSource,
            remote = remoteArticleDataSource,
        )

    @Provides
    fun providePackageInspector(
        @ApplicationContext context: Context,
    ): PackageInspector =
        AndroidPackageInspector(context.packageManager)

    @Provides
    fun provideBackendAvailabilityChecker(
        packageInspector: PackageInspector,
    ): BackendAvailabilityChecker =
        BackendAvailabilityChecker(packageInspector)
}
