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
import com.unitynews.news.domain.usecase.GetFilterSpecsUseCase
import com.unitynews.news.domain.usecase.ObserveArticlesUseCase
import com.unitynews.news.domain.usecase.RefreshArticlesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Production dependency graph for the reader app.
 *
 * The important direction is:
 * UI -> ViewModel -> UseCase -> NewsRepository -> Local/Remote data sources.
 * Keeping this wiring here makes it easy to swap the backend transport later
 * without changing the domain or presentation layers.
 */
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

    /**
     * Local source owns offline reads and writes.
     * The rest of the app only sees the NewsLocalDataSource interface.
     */
    @Provides
    fun provideNewsLocalDataSource(database: NewsDatabase): NewsLocalDataSource =
        RoomNewsLocalDataSource(database.newsDao())

    /**
     * The backend connection hides Android service binding details from the
     * repository. A future HTTP implementation can replace this binding here.
     */
    @Provides
    @Singleton
    fun provideBackendConnection(
        @ApplicationContext context: Context,
    ): BackendConnection =
        AndroidBackendConnection(context)

    /**
     * Remote source talks to the companion backend app through AIDL.
     */
    @Provides
    fun provideRemoteArticleDataSource(
        backendConnection: BackendConnection,
    ): RemoteArticleDataSource =
        AidlArticleDataSource(
            backend = backendConnection::connect,
        )

    /**
     * Repository is the one object that combines local cache + remote refresh.
     * ViewModels should not inject this directly; they should use use cases.
     */
    @Provides
    fun provideNewsRepository(
        localDataSource: NewsLocalDataSource,
        remoteArticleDataSource: RemoteArticleDataSource,
    ): NewsRepository =
        OfflineFirstNewsRepository(
            local = localDataSource,
            remote = remoteArticleDataSource,
        )

    /** Reads the currently cached articles for a filter. */
    @Provides
    fun provideObserveArticlesUseCase(repository: NewsRepository): ObserveArticlesUseCase =
        ObserveArticlesUseCase(repository)

    /** Refreshes the cache from the backend for a filter. */
    @Provides
    fun provideRefreshArticlesUseCase(repository: NewsRepository): RefreshArticlesUseCase =
        RefreshArticlesUseCase(repository)

    /** Loads backend-defined filters, so new filter types can be added server-side. */
    @Provides
    fun provideGetFilterSpecsUseCase(repository: NewsRepository): GetFilterSpecsUseCase =
        GetFilterSpecsUseCase(repository)

    /** Small wrapper around PackageManager so availability checks are testable. */
    @Provides
    fun providePackageInspector(
        @ApplicationContext context: Context,
    ): PackageInspector =
        AndroidPackageInspector(context.packageManager)

    /** Reports whether the companion backend package is installed. */
    @Provides
    fun provideBackendAvailabilityChecker(
        packageInspector: PackageInspector,
    ): BackendAvailabilityChecker =
        BackendAvailabilityChecker(packageInspector)
}
