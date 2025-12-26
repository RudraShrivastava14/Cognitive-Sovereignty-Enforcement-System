package com.example.attentiontokenmanager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_tokens")
data class AppTokenEntity(

    @PrimaryKey
    val packageName: String,

    val maxTokens: Int,

    val remainingTokens: Int,

    val lastUpdated: Long
)
