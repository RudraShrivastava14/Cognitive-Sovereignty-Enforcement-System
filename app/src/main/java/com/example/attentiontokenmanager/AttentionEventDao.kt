package com.example.attentiontokenmanager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.attentiontokenmanager.analytics.AppCount
import com.example.attentiontokenmanager.analytics.HourCount
import com.example.attentiontokenmanager.analytics.AppBlockStats

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

    @Query("""
        SELECT strftime('%H', timestamp / 1000, 'unixepoch') AS hour,
               COUNT(*) AS count
        FROM attention_events
        GROUP BY hour
        ORDER BY hour
    """)
    suspend fun eventsPerHourRaw(): List<HourCount>

    // Phase 7: AI Learning Analytics
    @Query("""
        SELECT COUNT(*) as totalEvents, 
               COALESCE(SUM(CASE WHEN allowed = 0 THEN 1 ELSE 0 END), 0) as blockedEvents
        FROM attention_events 
        WHERE packageName = :pkg AND timestamp >= :sinceTimestamp
    """)
    suspend fun getAppStatsSince(pkg: String, sinceTimestamp: Long): AppBlockStats

    @Query("""
        SELECT packageName AS packageName,
               COUNT(*) AS count
        FROM attention_events
        WHERE allowed = 0
          AND timestamp >= :startOfDay
          AND timestamp <= :endOfDay
        GROUP BY packageName
        ORDER BY count DESC
    """)
    suspend fun getBlockedAppsForDate(startOfDay: Long, endOfDay: Long): List<AppCount>

    @Query("DELETE FROM attention_events")
    suspend fun clearAllEvents()

    @Query("SELECT * FROM attention_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentEvents(limit: Int): List<AttentionEventEntity>

    // ── Phase 8 Insight Queries ───────────────────────────────────────────

    // Count time-restricted blocks in a time range (for focus_violation insight)
    @Query("""
        SELECT COUNT(*) FROM attention_events 
        WHERE reason = 'time_restricted' 
        AND timestamp >= :since 
        AND timestamp < :until
    """)
    suspend fun timeRestrictedCount(since: Long, until: Long): Int

    // Count blocked events in a time range (for daily_trend insight)
    @Query("""
        SELECT COUNT(*) FROM attention_events 
        WHERE allowed = 0 
        AND timestamp >= :since 
        AND timestamp < :until
    """)
    suspend fun blockedBetween(since: Long, until: Long): Int

    // Count total events in a time range (for streak insight)
    @Query("""
        SELECT COUNT(*) FROM attention_events 
        WHERE timestamp >= :since 
        AND timestamp < :until
    """)
    suspend fun totalEventsBetween(since: Long, until: Long): Int
}
