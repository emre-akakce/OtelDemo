package com.example.oteldemo.logger.infrastructure

/** PRV-03: known-sensitive key'lerin değerleri *** ile değiştirilir. */
internal object Masking {

    private val SENSITIVE_KEYS = setOf(
        "cookie",
        "logincookie",
        "appid",
        "email",
        "phone",
        "password",
        "authorization",
        "token",
    )

    fun apply(params: Map<String, Any?>): Map<String, Any?> {
        if (params.isEmpty()) return params
        val result = HashMap<String, Any?>(params.size)
        for ((k, v) in params) {
            result[k] = if (k.lowercase() in SENSITIVE_KEYS) "***" else v
        }
        return result
    }
}
