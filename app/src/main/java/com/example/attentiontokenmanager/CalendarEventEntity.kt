package com.example.attentiontokenmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,           // "2026-03-29"
    val eventType: String,      // "EXAM", "INTERNSHIP", "FOCUS"
    val eventName: String       // "Physics Final"
)
