package com.example.attentiontokenmanager

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.attentiontokenmanager.analytics.InsightDao
import com.example.attentiontokenmanager.analytics.InsightEntity

@Database(
    entities = [
        AppTokenEntity::class,
        AttentionEventEntity::class,
        CalendarEventEntity::class,
        InsightEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appTokenDao(): AppTokenDao
    abstract fun attentionEventDao(): AttentionEventDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun insightDao(): InsightDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "attention_token_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
