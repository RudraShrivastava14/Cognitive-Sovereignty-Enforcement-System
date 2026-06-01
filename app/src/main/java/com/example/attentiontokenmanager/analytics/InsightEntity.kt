package com.example.attentiontokenmanager.analytics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insights")
data class InsightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val generatedDate: Long,   // midnight timestamp of the day this insight belongs to
    val insightText: String,   // human-readable sentence shown to user
    val insightType: String    // "peak_hour" | "top_app" | "focus_violation" |
                               // "daily_trend" | "token_efficiency" | "streak"
)
