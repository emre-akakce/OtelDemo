package com.example.oteldemo.logger.infrastructure

import com.example.oteldemo.logger.application.Logger
import com.example.oteldemo.logger.domain.LogEntry
import com.example.oteldemo.logger.domain.LogLevel
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.logs.Severity
import java.time.Instant

internal class OtelLogger(
    private val category: String,
    private val state: LoggerState,
) : Logger {

    override fun log(
        level: LogLevel,
        template: String,
        params: Map<String, Any?>,
        error: Throwable?,
    ) {
        val entry = LogEntry(
            level = level,
            template = template,
            params = params,
            error = error,
            category = category,
            timestampMillis = System.currentTimeMillis(),
        )

        // 1. Logger henüz start edilmediyse buffer'a koy
        if (!state.started) {
            state.pending.add(entry)
            ConsoleSink.write(category, level, render(template, params), error)
            return
        }

        // 2. Level filtresi
        val cfg = state.config ?: return
        if (level.value < cfg.logLevel.value || level == LogLevel.None) return

        emit(entry)

        // 6. Error/Critical'da flush tetikle (TRN-03) — synchronously so logs survive if the process dies right after
        if (level.value >= LogLevel.Error.value) {
            state.otel?.forceFlush()
        }
    }

    private fun emit(entry: LogEntry) {
        val cfg = state.config ?: return
        val otel = state.otel ?: return
        val otelLogger = otel.loggerProvider.loggerFor(entry.category)

        // 3. Alert attribute (NTF-01)
        val alert = entry.level.value >= cfg.effectiveNotificationLevel.value

        // 4. Masking (PRV-03)
        val masked = Masking.apply(entry.params)

        val attrs = Attributes.builder()
            .put(AttributeKey.stringKey("logger.category"), entry.category)
            .put(AttributeKey.booleanKey("alert"), alert)
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

        otelLogger.logRecordBuilder()
            .setTimestamp(Instant.ofEpochMilli(entry.timestampMillis))
            .setSeverity(entry.level.toSeverity())
            .setSeverityText(entry.level.name)
            .setBody(render(entry.template, masked))
            .setAllAttributes(attrs)
            .emit()

        ConsoleSink.write(entry.category, entry.level, render(entry.template, masked), entry.error)
    }

    private fun render(template: String, params: Map<String, Any?>): String {
        if (params.isEmpty()) return template
        var out = template
        for ((k, v) in params) {
            out = out.replace("{$k}", v?.toString() ?: "null")
        }
        return out
    }
}

private fun LogLevel.toSeverity(): Severity = when (this) {
    LogLevel.Trace -> Severity.TRACE
    LogLevel.Debug -> Severity.DEBUG
    LogLevel.Information -> Severity.INFO
    LogLevel.Warning -> Severity.WARN
    LogLevel.Error -> Severity.ERROR
    LogLevel.Critical -> Severity.FATAL
    LogLevel.None -> Severity.UNDEFINED_SEVERITY_NUMBER
}
