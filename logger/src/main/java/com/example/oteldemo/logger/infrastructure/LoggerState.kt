package com.example.oteldemo.logger.infrastructure

import com.example.oteldemo.logger.domain.LoggingConfig
import java.util.concurrent.atomic.AtomicReference

/**
 * Tüm OtelLogger instance'larının paylaştığı tek state.
 * started = false iken loglar PendingLogBuffer'a yazılır (TRN-06).
 */
internal class LoggerState {
    private val configRef = AtomicReference<LoggingConfig?>(null)
    private val otelRef = AtomicReference<OtelSetup?>(null)
    private val appIdRef = AtomicReference<String?>(null)

    val pending: PendingLogBuffer = PendingLogBuffer()

    val config: LoggingConfig? get() = configRef.get()
    val otel: OtelSetup? get() = otelRef.get()
    val appId: String? get() = appIdRef.get()
    val started: Boolean get() = otelRef.get() != null && configRef.get() != null

    fun start(config: LoggingConfig, otel: OtelSetup) {
        configRef.set(config)
        otelRef.set(otel)
    }

    fun setAppId(appId: String) {
        appIdRef.set(appId)
    }
}
