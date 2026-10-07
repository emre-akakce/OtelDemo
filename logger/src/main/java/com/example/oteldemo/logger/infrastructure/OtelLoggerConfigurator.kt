package com.example.oteldemo.logger.infrastructure

import android.content.Context
import com.example.oteldemo.logger.application.LoggerConfigurator
import com.example.oteldemo.logger.domain.LogLevel
import com.example.oteldemo.logger.domain.LoggingConfig
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity
import java.time.Instant
import java.util.concurrent.TimeUnit

internal class OtelLoggerConfigurator(
    private val context: Context,
    private val state: LoggerState,
    private val serviceName: String,
    private val environment: String,
) : LoggerConfigurator {

    override fun start(config: LoggingConfig) {
        if (state.started) return

        val resourceInfo = ResourceInfo.build(context, serviceName, environment)
        val otel = OtelSetup.create(config.endpoint, resourceInfo)
        state.start(config, otel)

        drainPending(config)
    }

    override fun setAppId(appId: String) {
        state.setAppId(appId)
    }

    override fun flush() {
        state.otel?.forceFlush()
    }

    private fun drainPending(config: LoggingConfig) {
        val otel = state.otel ?: return
        val pending = state.pending.drain()
        if (pending.isEmpty()) return

        for (entry in pending) {
            if (entry.level.value < config.logLevel.value || entry.level == LogLevel.None) continue
            val alert = entry.level.value >= config.effectiveNotificationLevel.value
            val masked = Masking.apply(entry.params)

            val attrs = Attributes.builder()
                .put(AttributeKey.stringKey("logger.category"), entry.category)
                .put(AttributeKey.booleanKey("alert"), alert)
                .put(AttributeKey.booleanKey("pending.flushed"), true)
                .apply {
                    state.appId?.let { put(AttributeKey.stringKey("app.id"), it) }
                    for ((k, v) in masked) {
                        put(AttributeKey.stringKey("param.$k"), v?.toString() ?: "null")
                    }
                    entry.error?.let {
                        put(AttributeKey.stringKey("exception.type"), it.javaClass.name)
                        put(AttributeKey.stringKey("exception.message"), it.message ?: "")
                        put(AttributeKey.stringKey("exception.stacktrace"), it.stackTraceToString())
                    }
                }
                .build()

            otel.loggerProvider.loggerFor(entry.category).logRecordBuilder()
                .setTimestamp(Instant.ofEpochMilli(entry.timestampMillis))
                .setSeverity(entry.level.toSeverityForFlush())
                .setSeverityText(entry.level.name)
                .setBody(entry.template)
                .setAllAttributes(attrs)
                .emit()
        }
    }
}

private fun LogLevel.toSeverityForFlush(): Severity = when (this) {
    LogLevel.Trace -> Severity.TRACE
    LogLevel.Debug -> Severity.DEBUG
    LogLevel.Information -> Severity.INFO
    LogLevel.Warning -> Severity.WARN
    LogLevel.Error -> Severity.ERROR
    LogLevel.Critical -> Severity.FATAL
    LogLevel.None -> Severity.UNDEFINED_SEVERITY_NUMBER
}
