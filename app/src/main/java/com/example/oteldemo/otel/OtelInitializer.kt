package com.example.oteldemo.otel

import com.example.oteldemo.BuildConfig
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes

object OtelInitializer {

    @Volatile
    private var openTelemetry: OpenTelemetry? = null

    fun get(): OpenTelemetry = openTelemetry
        ?: error("OpenTelemetry not initialized — call OtelInitializer.init() in Application.onCreate")

    fun init(serviceName: String = "android-oteldemo") {
        if (openTelemetry != null) return

        val resource = Resource.getDefault().merge(
            Resource.create(
                Attributes.builder()
                    .put(ServiceAttributes.SERVICE_NAME, serviceName)
                    .put("deployment.environment", "local")
                    .build()
            )
        )

        val spanExporter = OtlpHttpSpanExporter.builder()
            .setEndpoint("${BuildConfig.OTEL_ENDPOINT}/v1/traces")
            .build()

        val logExporter = OtlpHttpLogRecordExporter.builder()
            .setEndpoint("${BuildConfig.OTEL_ENDPOINT}/v1/logs")
            .build()

        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .build()

        val loggerProvider = SdkLoggerProvider.builder()
            .setResource(resource)
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
            .build()

        openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setLoggerProvider(loggerProvider)
            .build()
    }
}
