package com.example.oteldemo.logger.domain

data class LoggingConfig(
    val logLevel: LogLevel,
    val notificationLevel: LogLevel,
    val endpoint: String,
) {
    // CFG-04: notification level, log level'in altında olamaz
    val effectiveNotificationLevel: LogLevel
        get() = if (notificationLevel.value < logLevel.value) logLevel else notificationLevel
}
