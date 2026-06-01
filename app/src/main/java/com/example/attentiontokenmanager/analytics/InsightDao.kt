package com.example.attentiontokenmanager.analytics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface InsightDao {

    @Insert
    suspend fun insertAll(insights: List<InsightEntity>)

    // Get all insights from last 5 days, newest first
    @Query("SELECT * FROM insights WHERE generatedDate >= :since ORDER BY generatedDate DESC")
    suspend fun getInsightsSince(since: Long): List<InsightEntity>

    // Get insights for a specific day
    @Query("SELECT * FROM insights WHERE generatedDate = :date ORDER BY id ASC")
    suspend fun getInsightsForDate(date: Long): List<InsightEntity>

    // Check if insights already generated for a given date (prevent duplicates)
    @Query("SELECT COUNT(*) FROM insights WHERE generatedDate = :date")
    suspend fun countForDate(date: Long): Int

    // Purge insights older than 5 days
    @Query("DELETE FROM insights WHERE generatedDate < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM insights")
    suspend fun clearAll()
}
