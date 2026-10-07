package com.example.oteldemo.logger.infrastructure

import android.util.Log
import com.example.oteldemo.logger.domain.LogLevel

/**
 * ARC-05: yalnızca debug build'de platform log'una yazar.
 * Enabled flag app tarafından set edilir (BuildConfig.DEBUG).
 */
internal object ConsoleSink {
    @Volatile var enabled: Boolean = false

    fun write(category: String, level: LogLevel, message: String, error: Throwable?) {
        if (!enabled) return
        val priority = when (level) {
            LogLevel.Trace -> Log.VERBOSE
            LogLevel.Debug -> Log.DEBUG
            LogLevel.Information -> Log.INFO
            LogLevel.Warning -> Log.WARN
            LogLevel.Error -> Log.ERROR
            LogLevel.Critical -> Log.ASSERT
            LogLevel.None -> return
        }
        if (error != null) {
            Log.println(priority, category, "$message\n${Log.getStackTraceString(error)}")
        } else {
            Log.println(priority, category, message)
        }
    }
}
