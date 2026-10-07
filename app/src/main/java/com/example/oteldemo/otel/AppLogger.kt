package com.example.oteldemo.otel

import android.util.Log
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity

enum class Level(val severity: Severity, val androidPriority: Int) {
    TRACE(Severity.TRACE, Log.VERBOSE),
    DEBUG(Severity.DEBUG, Log.DEBUG),
    INFO(Severity.INFO, Log.INFO),
    WARN(Severity.WARN, Log.WARN),
    ERROR(Severity.ERROR, Log.ERROR),
    CRITICAL(Severity.FATAL, Log.ASSERT),
}

data class LogDetails(
    val title: String? = null,
    val message: String,
    val endpoint: String? = null,
    val status: Int? = null,
    val durationMs: Long? = null,
    val stacktrace: String? = null,
)

class AppLogger(private val source: String) {
    private val otelLogger = OtelInitializer.get().logsBridge.get(source)

    fun log(level: Level, details: LogDetails) {
        // Layer 1 — original Android logging
        Log.println(level.androidPriority, source, "${details.title ?: "-"}: ${details.message}")

        // Layer 2 — forward to OTel pipeline
        val attrs = Attributes.builder()
            .put(AttributeKey.stringKey("service.name"), source)
            .apply {
                details.title?.let     { put(AttributeKey.stringKey("log.title"), it) }
                details.endpoint?.let  { put(AttributeKey.stringKey("log.endpoint"), it) }
                details.status?.let    { put(AttributeKey.longKey("log.status"), it.toLong()) }
                details.durationMs?.let{ put(AttributeKey.longKey("log.duration_ms"), it) }
                details.stacktrace?.let{ put(AttributeKey.stringKey("log.stacktrace"), it) }
            }
            .build()

        otelLogger.logRecordBuilder()
            .setSeverity(level.severity)
            .setSeverityText(level.name)
            .setBody(details.message)
            .setAllAttributes(attrs)
            .emit()
    }

    fun info(details: LogDetails)  = log(Level.INFO, details)
    fun warn(details: LogDetails)  = log(Level.WARN, details)
    fun error(details: LogDetails) = log(Level.ERROR, details)
}
