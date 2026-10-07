package com.example.oteldemo.logger.infrastructure

import io.opentelemetry.api.logs.Logger as OtelApiLogger
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.util.concurrent.TimeUnit

/**
 * TRN-01, TRN-02, TRN-04: OTLP/HTTP exporters + bounded in-memory queue processors.
 */
internal class OtelSetup private constructor(
    val loggerProvider: SdkLoggerProvider,
    val tracerProvider: SdkTracerProvider,
) {
    fun shutdown() {
        loggerProvider.shutdown().join(5, TimeUnit.SECONDS)
        tracerProvider.shutdown().join(5, TimeUnit.SECONDS)
    }

    fun forceFlush(): Long {
        val a = loggerProvider.forceFlush().join(2, TimeUnit.SECONDS)
        val b = tracerProvider.forceFlush().join(2, TimeUnit.SECONDS)
        return 0
    }

    companion object {
        fun create(endpoint: String, resourceInfo: ResourceInfo): OtelSetup {
            val resource = Resource.create(resourceInfo.attributes)

            val logExporter = OtlpHttpLogRecordExporter.builder()
                .setEndpoint("$endpoint/v1/logs")
                .setTimeout(10, TimeUnit.SECONDS)
                .build()

            val logProcessor = BatchLogRecordProcessor.builder(logExporter)
                .setMaxQueueSize(2048)
                .setMaxExportBatchSize(512)
                .setScheduleDelay(1, TimeUnit.SECONDS)
                .build()

            val loggerProvider = SdkLoggerProvider.builder()
                .setResource(resource)
                .addLogRecordProcessor(logProcessor)
                .build()

            val spanExporter = OtlpHttpSpanExporter.builder()
                .setEndpoint("$endpoint/v1/traces")
                .setTimeout(10, TimeUnit.SECONDS)
                .build()

            val spanProcessor = BatchSpanProcessor.builder(spanExporter)
                .setMaxQueueSize(2048)
                .setMaxExportBatchSize(512)
                .setScheduleDelay(1, TimeUnit.SECONDS)
                .build()

            val tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(spanProcessor)
                .build()

            return OtelSetup(loggerProvider, tracerProvider)
        }
    }
}

internal fun SdkLoggerProvider.loggerFor(category: String): OtelApiLogger = get(category)
internal fun SdkTracerProvider.tracerFor(category: String): Tracer = get(category)
