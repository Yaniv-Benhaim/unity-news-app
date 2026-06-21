package com.unitynews.news.presentation.di

import com.unitynews.news.presentation.model.AndroidNewsTextProvider
import com.unitynews.news.presentation.model.NewsTextProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Presentation-only bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
interface NewsPresentationModule {
    @Binds
    fun bindNewsTextProvider(provider: AndroidNewsTextProvider): NewsTextProvider
}
