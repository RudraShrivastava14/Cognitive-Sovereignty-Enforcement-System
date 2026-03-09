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

    private val tokenMap = mutableMapOf<String, Int>()
    private val maxTokenMap = mutableMapOf<String, Int>()

    private val DEFAULT_DAILY_TOKENS = 5

    private lateinit var database: AppDatabase
    private lateinit var eventDao: AttentionEventDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        var instance: AttentionManagerService? = null
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        // Initialize Room database
        database = AppDatabase.getInstance(applicationContext)
        eventDao = database.attentionEventDao()

        startForegroundImmediately()

        android.util.Log.d("AttentionManager", "Service created")
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

    private fun getRemainingTokens(pkg: String): Int {
        if (!tokenMap.containsKey(pkg)) {

            val max = maxTokenMap[pkg] ?: DEFAULT_DAILY_TOKENS

            maxTokenMap[pkg] = max
            tokenMap[pkg] = max
        }

        return tokenMap[pkg]!!
    }

    fun handleNotification(packageName: String): Boolean {

        val remaining = getRemainingTokens(packageName)

        val allowed = remaining > 0

        if (allowed) {
            tokenMap[packageName] = remaining - 1
        }

        val remainingAfter = tokenMap[packageName] ?: 0

        android.util.Log.d(
            "AttentionManager",
            "Notification from $packageName allowed=$allowed remaining=$remainingAfter"
        )

        // Log analytics event
        serviceScope.launch {
            try {

                eventDao.insert(
                    AttentionEventEntity(
                        packageName = packageName,
                        timestamp = System.currentTimeMillis(),
                        allowed = allowed,
                        remainingTokens = remainingAfter,
                        windowStart = 0L,
                        reason = if (allowed) "allowed" else "tokens_exhausted"
                    )
                )

            } catch (e: Exception) {

                android.util.Log.e(
                    "AttentionManager",
                    "Failed to log analytics event",
                    e
                )
            }
        }

        return allowed
    }

    fun setMaxTokens(pkg: String, max: Int) {

        maxTokenMap[pkg] = max
        tokenMap[pkg] = max

        android.util.Log.d(
            "AttentionManager",
            "Max tokens updated for $pkg = $max"
        )
    }

    fun getMaxTokens(pkg: String): Int {
        return maxTokenMap[pkg] ?: DEFAULT_DAILY_TOKENS
    }

    fun getRemainingTokensPublic(pkg: String): Int {
        return tokenMap[pkg] ?: DEFAULT_DAILY_TOKENS
    }

    fun resetAllTokens() {

        for ((pkg, max) in maxTokenMap) {
            tokenMap[pkg] = max
        }

        android.util.Log.d(
            "AttentionManager",
            "All tokens reset"
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()

        android.util.Log.d(
            "AttentionManager",
            "Service destroyed"
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
