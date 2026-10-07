package com.example.oteldemo

import android.app.Application
import com.example.oteldemo.logger.LoggerModule
import com.example.oteldemo.logger.application.LoggerConfigurator
import com.example.oteldemo.logger.application.LoggerFactory
import com.example.oteldemo.logger.application.TracerFactory
import com.example.oteldemo.logger.domain.LogLevel
import com.example.oteldemo.logger.domain.LoggingConfig

class OtelApplication : Application() {

    lateinit var loggerFactory: LoggerFactory
        private set
    lateinit var loggerConfigurator: LoggerConfigurator
        private set
    lateinit var tracerFactory: TracerFactory
        private set

    override fun onCreate() {
        super.onCreate()

        val setup = LoggerModule.install(
            context = this,
            serviceName = "android-oteldemo",
            environment = "local",
            debug = BuildConfig.DEBUG,
        )
        loggerFactory = setup.factory
        loggerConfigurator = setup.configurator
        tracerFactory = setup.tracerFactory

        // In a real app, LoggingConfig gelirdi Remote Config'ten. Burada hard-code.
        loggerConfigurator.start(
            LoggingConfig(
                logLevel = LogLevel.Trace,
                notificationLevel = LogLevel.Error,
                endpoint = BuildConfig.OTEL_ENDPOINT,
            )
        )
        loggerConfigurator.setAppId("demo-app-id")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // TRN-05: Background'a geçerken flush
        if (level >= TRIM_MEMORY_BACKGROUND) loggerConfigurator.flush()
    }
}
