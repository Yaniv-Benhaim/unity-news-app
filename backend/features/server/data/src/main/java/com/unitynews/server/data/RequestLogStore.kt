package com.unitynews.server.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory request log shown by the backend console.
 *
 * Logs are intentionally capped so a long demo session does not grow memory
 * forever.
 */
class RequestLogStore(
    private val maxEntries: Int = 50,
) {
    private val timestampFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val _logs = MutableStateFlow<List<String>>(emptyList())

    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    /** Add a timestamped log entry to the top of the list. */
    fun add(message: String) {
        val timestamp = synchronized(timestampFormat) {
            timestampFormat.format(Date())
        }
        _logs.update { current ->
            (listOf("[$timestamp] $message") + current).take(maxEntries)
        }
    }

    /** Clear all visible request history. */
    fun clear() {
        _logs.value = emptyList()
    }
}
