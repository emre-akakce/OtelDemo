package com.example.oteldemo.logger.application

import com.example.oteldemo.logger.domain.LoggingConfig

interface LoggerConfigurator {
    fun start(config: LoggingConfig)   // Remote Config aktif edildikten sonra
    fun setAppId(appId: String)        // Application Init sonrası
    fun flush()                        // Background'a geçerken (TRN-05)
}
