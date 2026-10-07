package com.example.oteldemo.logger.domain

data class LogEntry(
    val level: LogLevel,
    val template: String,
    val params: Map<String, Any?>,
    val error: Throwable?,
    val category: String,
    val timestampMillis: Long,
)
