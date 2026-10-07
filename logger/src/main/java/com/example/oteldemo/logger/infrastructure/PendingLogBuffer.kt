package com.example.oteldemo.logger.infrastructure

import com.example.oteldemo.logger.domain.LogEntry
import java.util.ArrayDeque

/**
 * TRN-06: Logger start() çağrılmadan önce yazılan log'ları bellek sınırlı bir buffer'da tutar.
 * start() çalışınca drain edilip filtrelenerek emit edilir.
 */
internal class PendingLogBuffer(private val capacity: Int = 200) {
    private val queue = ArrayDeque<LogEntry>()

    @Synchronized
    fun add(entry: LogEntry) {
        if (queue.size >= capacity) queue.removeFirst()
        queue.addLast(entry)
    }

    @Synchronized
    fun drain(): List<LogEntry> {
        val snapshot = queue.toList()
        queue.clear()
        return snapshot
    }
}
