package com.example.oteldemo.logger.infrastructure

import com.example.oteldemo.logger.application.TracerFactory
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerProvider

internal class OtelTracerFactory(private val state: LoggerState) : TracerFactory {
    override fun get(category: String): Tracer {
        val provider: TracerProvider = state.otel?.tracerProvider ?: TracerProvider.noop()
        return provider.get(category)
    }
}
