package com.unitynews.news.presentation.model

/**
 * Provides user-facing copy to presentation classes that are not composables.
 *
 * Compose UI can call stringResource directly. ViewModels cannot, so they
 * depend on this small interface instead of hard-coded strings or Android
 * Context.
 */
interface NewsTextProvider {
    val unableToLoadArticles: String
    val unableToRefreshArticles: String
    val installOrStartBackend: String
}
