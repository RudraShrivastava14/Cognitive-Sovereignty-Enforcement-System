package com.example.attentiontokenmanager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertToken(appToken: AppTokenEntity)

    @Query("SELECT * FROM app_tokens WHERE packageName = :pkg LIMIT 1")
    suspend fun getToken(pkg: String): AppTokenEntity?

    @Query("SELECT * FROM app_tokens")
    suspend fun getAllTokens(): List<AppTokenEntity>

    @Query("UPDATE app_tokens SET remainingTokens = :remaining WHERE packageName = :pkg")
    suspend fun updateRemaining(pkg: String, remaining: Int)

    @Query("DELETE FROM app_tokens")
    suspend fun clearAll()
}