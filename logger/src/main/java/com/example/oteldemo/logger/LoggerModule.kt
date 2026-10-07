package com.example.oteldemo.logger

import android.content.Context
import com.example.oteldemo.logger.application.LoggerConfigurator
import com.example.oteldemo.logger.application.LoggerFactory
import com.example.oteldemo.logger.application.TracerFactory
import com.example.oteldemo.logger.infrastructure.ConsoleSink
import com.example.oteldemo.logger.infrastructure.LoggerState
import com.example.oteldemo.logger.infrastructure.OtelLoggerConfigurator
import com.example.oteldemo.logger.infrastructure.OtelLoggerFactory
import com.example.oteldemo.logger.infrastructure.OtelTracerFactory

object LoggerModule {

    data class Setup(
        val factory: LoggerFactory,
        val configurator: LoggerConfigurator,
        val tracerFactory: TracerFactory,
    )

    fun install(
        context: Context,
        serviceName: String,
        environment: String = "local",
        debug: Boolean = false,
    ): Setup {
        ConsoleSink.enabled = debug
        val state = LoggerState()
        val factory = OtelLoggerFactory(state)
        val tracerFactory = OtelTracerFactory(state)
        val configurator = OtelLoggerConfigurator(
            context.applicationContext,
            state,
            serviceName,
            environment,
        )
        return Setup(factory, configurator, tracerFactory)
    }
}
