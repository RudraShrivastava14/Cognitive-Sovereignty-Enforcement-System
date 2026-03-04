package com.example.attentiontokenmanager.analytics

import com.example.attentiontokenmanager.AttentionEventDao

class AnalyticsRepository(
    private val attentionEventDao: AttentionEventDao
) {

    suspend fun loadSummary(): AnalyticsSummary {
        val total = attentionEventDao.totalEvents()
        val blocked = attentionEventDao.totalBlocked()
        val allowed = attentionEventDao.totalAllowed()

        

        val blockRate =
            if (total == 0) 0f
            else blocked.toFloat() / total.toFloat()

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
}
