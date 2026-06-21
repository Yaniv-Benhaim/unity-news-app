package com.unitynews.news.presentation.model

/**
 * Prefer the specific backend/local error message, but keep a stable fallback.
 */
fun Throwable.readableMessage(fallback: String): String =
    message?.takeIf { it.isNotBlank() } ?: fallback
