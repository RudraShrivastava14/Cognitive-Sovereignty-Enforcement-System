package com.example.attentiontokenmanager.analytics

import com.example.attentiontokenmanager.AttentionEventDao

class AnalyticsRepository(
    private val attentionEventDao: AttentionEventDao
) {

    suspend fun loadSummary(): AnalyticsSummary {

        val total = attentionEventDao.totalEvents()
        val blocked = attentionEventDao.totalBlocked()
        val allowed = attentionEventDao.totalAllowed()

        val blockRate = if (total == 0) {
            0f
        } else {
            blocked.toFloat() / total.toFloat()
        }

        return AnalyticsSummary(
            totalEvents = total,
            blockedEvents = blocked,
            allowedEvents = allowed,
            blockRate = blockRate
        )
    }

    suspend fun loadBlockedByApp(): List<AppCount> {
        return attentionEventDao.blockedPerApp()
    }

    // 🔥 NEW: Hourly data for TimelineChart
    suspend fun loadEventsPerHour(): List<Int> {

        val raw = attentionEventDao.eventsPerHourRaw()

        // Create 24-hour list initialized with 0
        val result = MutableList(24) { 0 }

        raw.forEach {
            val hourIndex = it.hour.toIntOrNull() ?: 0
            if (hourIndex in 0..23) {
                result[hourIndex] = it.count
            }
        }

        return result
    }
}