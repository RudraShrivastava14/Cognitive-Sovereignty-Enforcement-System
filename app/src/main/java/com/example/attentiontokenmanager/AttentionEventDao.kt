package com.example.attentiontokenmanager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.attentiontokenmanager.analytics.AppCount
import com.example.attentiontokenmanager.analytics.HourCount

@Dao
interface AttentionEventDao {

    @Insert
    suspend fun insert(event: AttentionEventEntity)

    @Query("SELECT COUNT(*) FROM attention_events")
    suspend fun totalEvents(): Int

    @Query("SELECT COUNT(*) FROM attention_events WHERE allowed = 0")
    suspend fun totalBlocked(): Int

    @Query("SELECT COUNT(*) FROM attention_events WHERE allowed = 1")
    suspend fun totalAllowed(): Int

    @Query("""
        SELECT packageName AS packageName,
               COUNT(*) AS count
        FROM attention_events
        WHERE allowed = 0
        GROUP BY packageName
        ORDER BY count DESC
    """)
    suspend fun blockedPerApp(): List<AppCount>

    // 🔥 NEW: Hourly analytics
    @Query("""
        SELECT strftime('%H', timestamp / 1000, 'unixepoch') AS hour,
               COUNT(*) AS count
        FROM attention_events
        GROUP BY hour
        ORDER BY hour
    """)
    suspend fun eventsPerHourRaw(): List<HourCount>
}