package com.example.attentiontokenmanager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AttentionEventDao {

    @Insert
    suspend fun insert(event: AttentionEventEntity)

    @Query("SELECT COUNT(*) FROM attention_events")
    suspend fun totalEvents(): Int

    @Query("SELECT COUNT(*) FROM attention_events WHERE allowed = 1")
    suspend fun allowedEvents(): Int

    @Query("SELECT COUNT(*) FROM attention_events WHERE allowed = 0")
    suspend fun blockedEvents(): Int

    @Query("""
        SELECT packageName, COUNT(*) as count
        FROM attention_events
        WHERE allowed = 0
        GROUP BY packageName
    """)
    suspend fun blockedByApp(): List<AppCount>
}