package com.example.oteldemo.logger.application

import com.example.oteldemo.logger.domain.LogLevel

interface Logger {
    fun log(
        level: LogLevel,
        template: String,
        params: Map<String, Any?> = emptyMap(),
        error: Throwable? = null,
    )

    fun trace(template: String, vararg params: Pair<String, Any?>) =
        log(LogLevel.Trace, template, params.toMap())

    fun debug(template: String, vararg params: Pair<String, Any?>) =
        log(LogLevel.Debug, template, params.toMap())

    fun information(template: String, vararg params: Pair<String, Any?>) =
        log(LogLevel.Information, template, params.toMap())

    fun warning(template: String, vararg params: Pair<String, Any?>, error: Throwable? = null) =
        log(LogLevel.Warning, template, params.toMap(), error)

    fun error(template: String, vararg params: Pair<String, Any?>, error: Throwable? = null) =
        log(LogLevel.Error, template, params.toMap(), error)

    fun critical(template: String, vararg params: Pair<String, Any?>, error: Throwable? = null) =
        log(LogLevel.Critical, template, params.toMap(), error)
}
