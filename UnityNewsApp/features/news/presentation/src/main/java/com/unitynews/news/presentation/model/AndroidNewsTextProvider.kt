package com.unitynews.news.presentation.model

import android.content.Context
import com.unitynews.news.presentation.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Production string provider backed by Android resources.
 */
class AndroidNewsTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : NewsTextProvider {
    override val unableToLoadArticles: String
        get() = context.getString(R.string.news_error_unable_to_load_articles)

    override val unableToRefreshArticles: String
        get() = context.getString(R.string.news_error_unable_to_refresh_articles)

    override val installOrStartBackend: String
        get() = context.getString(R.string.news_error_install_or_start_backend)
}
