package com.example.attentiontokenmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attention_events")
data class AttentionEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val timestamp: Long,
    val allowed: Boolean,
    val remainingTokens: Int,
    val windowStart: Long,
    val reason: String? = null
)
