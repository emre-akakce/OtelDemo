package com.example.oteldemo.logger.application

interface LoggerFactory {
    fun create(category: String): Logger
}
