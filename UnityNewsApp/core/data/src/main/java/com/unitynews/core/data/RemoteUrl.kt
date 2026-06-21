package com.unitynews.core.data

/**
 * Returns a remote URL that image/network components can safely attempt to load.
 *
 * Local file/content URLs are intentionally rejected because article images are
 * external content controlled by the backend dataset, not local app resources.
 */
fun String.supportedRemoteUrlOrNull(): String? {
    val trimmed = trim()
    return trimmed.takeIf {
        it.startsWith("https://", ignoreCase = true) ||
            it.startsWith("http://", ignoreCase = true)
    }
}
