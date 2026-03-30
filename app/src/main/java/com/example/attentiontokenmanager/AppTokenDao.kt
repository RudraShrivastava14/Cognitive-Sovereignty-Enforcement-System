package com.example.attentiontokenmanager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface AppTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertToken(appToken: AppTokenEntity)

    @Query("SELECT * FROM app_tokens WHERE packageName = :pkg LIMIT 1")
    suspend fun getToken(pkg: String): AppTokenEntity?

    @Query("SELECT * FROM app_tokens")
    suspend fun getAllTokens(): List<AppTokenEntity>

    @Query("SELECT * FROM app_tokens")
    fun getAllTokensFlow(): Flow<List<AppTokenEntity>>

    @Delete
    suspend fun deleteToken(appToken: AppTokenEntity)

    @Query("UPDATE app_tokens SET remainingTokens = :remaining WHERE packageName = :pkg")
    suspend fun updateRemaining(pkg: String, remaining: Int)

    @Query("""
        UPDATE app_tokens 
        SET allowedStartHour = :startHour,
            allowedEndHour = :endHour,
            isAdaptiveLearningEnabled = :adaptiveEnabled
        WHERE packageName = :pkg
    """)
    suspend fun updateRules(
        pkg: String,
        startHour: Int?,
        endHour: Int?,
        adaptiveEnabled: Boolean
    )

    @Query("DELETE FROM app_tokens")
    suspend fun clearAll()
}