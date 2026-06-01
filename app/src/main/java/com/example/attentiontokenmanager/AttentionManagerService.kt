package com.example.attentiontokenmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class AttentionManagerService : Service() {

    private val DEFAULT_DAILY_TOKENS = 5

    private lateinit var database: AppDatabase
    private lateinit var eventDao: AttentionEventDao
    private lateinit var tokenDao: AppTokenDao

    companion object {
        var instance: AttentionManagerService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(applicationContext)
        eventDao = database.attentionEventDao()
        tokenDao = database.appTokenDao()

        // Seed default tracked apps if DB is empty
        runBlocking {
            val existing = tokenDao.getAllTokens()
            if (existing.isEmpty()) {
                val defaults = listOf(
                    "com.whatsapp",
                    "com.instagram.android",
                    "com.google.android.gm"
                )
                val start = TimeWindow.currentWindowStart()
                defaults.forEach { pkg ->
                    tokenDao.upsertToken(
                        AppTokenEntity(
                            packageName = pkg,
                            maxTokens = 5,
                            remainingTokens = 5,
                            lastUpdated = start
                        )
                    )
                }
            }
        }

        startForegroundImmediately()
        android.util.Log.d("AttentionManager", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startForegroundImmediately() {
        val channelId = "attention_manager_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Attention Manager",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Attention Manager Active")
            .setContentText("Managing attention tokens")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    fun handleNotification(packageName: String): Boolean {
        return runBlocking {
            try {
                val currentWindow = TimeWindow.currentWindowStart()
                var token = tokenDao.getToken(packageName)

                // Only manage explicitly tracked apps
                if (token == null) return@runBlocking true

                // Daily reset logic
                if (token.lastUpdated != currentWindow) {
                    token = token.copy(
                        remainingTokens = token.maxTokens,
                        lastUpdated = currentWindow
                    )
                    tokenDao.upsertToken(token)
                    android.util.Log.d("AttentionManager", "Tokens reset for $packageName")
                }

                // Time-based restrictions
                val currentHour = java.util.Calendar.getInstance()
                    .get(java.util.Calendar.HOUR_OF_DAY)
                val hasStartLimit = token.allowedStartHour != null
                val hasEndLimit = token.allowedEndHour != null

                var isTimeRestricted = false
                if (hasStartLimit && hasEndLimit &&
                    token.allowedStartHour != token.allowedEndHour) {
                    if (token.allowedStartHour!! <= token.allowedEndHour!!) {
                        isTimeRestricted = currentHour < token.allowedStartHour!! ||
                                currentHour >= token.allowedEndHour!!
                    } else {
                        isTimeRestricted = currentHour >= token.allowedEndHour!! &&
                                currentHour < token.allowedStartHour!!
                    }
                }

                if (isTimeRestricted) {
                    android.util.Log.d(
                        "AttentionManager",
                        "Notification from $packageName blocked (time_restricted)"
                    )
                    eventDao.insert(
                        AttentionEventEntity(
                            packageName = packageName,
                            timestamp = System.currentTimeMillis(),
                            allowed = false,
                            remainingTokens = token.remainingTokens,
                            windowStart = currentWindow,
                            reason = "time_restricted"
                        )
                    )
                    return@runBlocking false
                }

                val remaining = token.remainingTokens
                val allowed = remaining > 0
                val remainingAfter = if (allowed) remaining - 1 else remaining

                if (allowed) tokenDao.updateRemaining(packageName, remainingAfter)

                android.util.Log.d(
                    "AttentionManager",
                    "Notification from $packageName allowed=$allowed remaining=$remainingAfter"
                )

                eventDao.insert(
                    AttentionEventEntity(
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        allowed = allowed,
                        remainingTokens = remainingAfter,
                        windowStart = currentWindow,
                        reason = if (allowed) "allowed" else "tokens_exhausted"
                    )
                )

                allowed

            } catch (e: Exception) {
                android.util.Log.e("AttentionManager", "Error handling notification", e)
                true
            }
        }
    }

    fun setMaxTokens(pkg: String, max: Int) {
        runBlocking {
            val token = tokenDao.getToken(pkg)
            if (token != null) {
                tokenDao.upsertToken(
                    token.copy(
                        maxTokens = max,
                        remainingTokens = max,
                        lastUpdated = token.lastUpdated  // preserve existing window
                    )
                )
            } else {
                // ✅ FIX 3: New tokens use midnight timestamp so daily reset
                // logic doesn't fire on every single notification after re-add
                tokenDao.upsertToken(
                    AppTokenEntity(
                        packageName = pkg,
                        maxTokens = max,
                        remainingTokens = max,
                        lastUpdated = TimeWindow.currentWindowStart()
                    )
                )
            }
        }
        android.util.Log.d("AttentionManager", "Max tokens updated for $pkg = $max")
    }

    fun deleteTokenConfig(pkg: String) {
        runBlocking {
            val token = tokenDao.getToken(pkg)
            if (token != null) {
                tokenDao.deleteToken(token)
            }
        }
    }

    fun getMaxTokens(pkg: String): Int {
        return runBlocking {
            val token = tokenDao.getToken(pkg)
            token?.maxTokens ?: DEFAULT_DAILY_TOKENS
        }
    }

    fun getRemainingTokensPublic(pkg: String): Int {
        return runBlocking {
            val token = tokenDao.getToken(pkg)
            token?.remainingTokens ?: DEFAULT_DAILY_TOKENS
        }
    }

    fun getTokenConfig(pkg: String): AppTokenEntity? {
        return runBlocking {
            tokenDao.getToken(pkg)
        }
    }

    fun updateTokenConfig(
        pkg: String,
        isAdaptiveEnabled: Boolean,
        startHour: Int,
        endHour: Int
    ) {
        runBlocking {
            val token = tokenDao.getToken(pkg)
            if (token != null) {
                tokenDao.updateRules(pkg, startHour, endHour, isAdaptiveEnabled)
            } else {
                tokenDao.upsertToken(
                    AppTokenEntity(
                        packageName = pkg,
                        maxTokens = DEFAULT_DAILY_TOKENS,
                        remainingTokens = DEFAULT_DAILY_TOKENS,
                        allowedStartHour = startHour,
                        allowedEndHour = endHour,
                        isAdaptiveLearningEnabled = isAdaptiveEnabled,
                        lastUpdated = TimeWindow.currentWindowStart()
                    )
                )
            }
        }
    }

    fun resetAllTokens() {
        runBlocking {
            val allTokens = tokenDao.getAllTokens()
            allTokens.forEach { token ->
                tokenDao.updateRemaining(token.packageName, token.maxTokens)
            }
        }
        android.util.Log.d("AttentionManager", "All tokens reset")
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("AttentionManager", "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
