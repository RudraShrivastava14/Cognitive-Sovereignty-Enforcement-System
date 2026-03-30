package com.example.attentiontokenmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_tokens")
data class AppTokenEntity(

    @PrimaryKey
    val packageName: String,

    val maxTokens: Int,

    val remainingTokens: Int,

    val lastUpdated: Long,

    // Phase 7: Smart Rules & AI
    val allowedStartHour: Int? = null,

    val allowedEndHour: Int? = null,

    val isAdaptiveLearningEnabled: Boolean = true,

    val dailyAdjustmentLog: String? = null
)