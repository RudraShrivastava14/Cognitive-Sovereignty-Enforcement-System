package com.example.attentiontokenmanager

class AnalyticsRepository(
    private val eventDao: AttentionEventDao
) {

    // 1️⃣ Total notifications
    suspend fun totalEvents(): Int {
        return eventDao.totalEvents()
    }

    // 2️⃣ Total blocked
    suspend fun blockedEvents(): Int {
        return eventDao.blockedEvents()
    }

    // 3️⃣ Total allowed
    suspend fun allowedEvents(): Int {
        return eventDao.allowedEvents()
    }

    // 4️⃣ Block rate (0.0 → 1.0)
    suspend fun blockRate(): Float {
        val total = totalEvents()
        if (total == 0) return 0f
        return blockedEvents().toFloat() / total
    }

    // 5️⃣ Blocked notifications per app (for charts)
    suspend fun blockedByApp(): List<AppCount> {
        return eventDao.blockedByApp()
    }
}