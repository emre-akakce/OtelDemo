package com.example.oteldemo.logger.application

import io.opentelemetry.api.trace.Tracer

interface TracerFactory {
    fun get(category: String): Tracer
}
