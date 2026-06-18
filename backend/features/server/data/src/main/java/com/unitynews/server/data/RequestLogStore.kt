package com.unitynews.server.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RequestLogStore(
    private val maxEntries: Int = 50,
) {
    private val timestampFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val _logs = MutableStateFlow<List<String>>(emptyList())

    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun add(message: String) {
        val timestamp = synchronized(timestampFormat) {
            timestampFormat.format(Date())
        }
        _logs.update { current ->
            (listOf("[$timestamp] $message") + current).take(maxEntries)
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
