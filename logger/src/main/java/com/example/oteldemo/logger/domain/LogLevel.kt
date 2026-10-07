package com.example.oteldemo.logger.domain

enum class LogLevel(val value: Int) {
    Trace(0),
    Debug(1),
    Information(2),
    Warning(3),
    Error(4),
    Critical(5),
    None(6);

    companion object {
        // CFG-03: Remote Config değerinin parse edilmesi, bilinmeyen değerlerde default'a düşer
        fun parse(raw: String?, default: LogLevel): LogLevel =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: default
    }
}
