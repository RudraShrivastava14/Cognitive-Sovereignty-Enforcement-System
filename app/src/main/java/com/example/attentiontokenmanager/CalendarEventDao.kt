package com.example.attentiontokenmanager

import androidx.room.*

@Dao
interface CalendarEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEventEntity)

    @Query("SELECT * FROM calendar_events WHERE date = :date")
    suspend fun getEventsForDate(date: String): List<CalendarEventEntity>

    @Query("SELECT * FROM calendar_events WHERE date >= :startDate AND date <= :endDate")
    suspend fun getEventsInRange(startDate: String, endDate: String): List<CalendarEventEntity>

    @Query("SELECT DISTINCT date FROM calendar_events")
    suspend fun getAllEventDates(): List<String>

    @Delete
    suspend fun delete(event: CalendarEventEntity)
}
