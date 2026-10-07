package com.example.oteldemo.logger.infrastructure

import com.example.oteldemo.logger.application.Logger
import com.example.oteldemo.logger.application.LoggerFactory

internal class OtelLoggerFactory(private val state: LoggerState) : LoggerFactory {
    override fun create(category: String): Logger = OtelLogger(category, state)
}
