package com.example.attentiontokenmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class AttentionManagerService : Service() {

    private val tokenMap = mutableMapOf<String, Int>()
    private val maxTokenMap = mutableMapOf<String, Int>()
    private val DEFAULT_DAILY_TOKENS = 5



    private lateinit var database: AppDatabase
    private lateinit var tokenDao: AppTokenDao
    private lateinit var eventDao: AttentionEventDao   // ✅ Phase-6B

    companion object {
        var instance: AttentionManagerService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // ✅ Correct initialization order
        database = AppDatabase.getInstance(applicationContext)
        tokenDao = database.appTokenDao()
        eventDao = database.attentionEventDao()

        startForegroundImmediately()

        // Load persisted tokens
        CoroutineScope(Dispatchers.IO).launch {
            val storedTokens = tokenDao.getAllTokens()
            for (token in storedTokens) {
                maxTokenMap[token.packageName] = token.maxTokens
                tokenMap[token.packageName] = token.remainingTokens
            }
        }
    }

    // ✅ Phase-6B Event Logger
    private fun logEvent(
        pkg: String,
        allowed: Boolean,
        remaining: Int,
        reason: String? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            eventDao.insert(
                AttentionEventEntity(
                    packageName = pkg,
                    timestamp = System.currentTimeMillis(),
                    allowed = allowed,
                    remainingTokens = remaining,
                    windowStart = TimeWindow.currentWindowStart(),
                    reason = reason
                )
            )
        }
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

        return if (remaining > 0) {

            // ✅ ALLOWED
            tokenMap[packageName] = remaining - 1

            CoroutineScope(Dispatchers.IO).launch {
                tokenDao.updateRemaining(packageName, remaining - 1)
            }

            logEvent(
                pkg = packageName,
                allowed = true,
                remaining = remaining - 1
            )

            Log.d("AttentionManager", "ALLOWED: $packageName | tokens left=${remaining - 1}")
            true

        } else {

            // ❌ BLOCKED
            logEvent(
                pkg = packageName,
                allowed = false,
                remaining = 0,
                reason = "No tokens left"
            )

            Log.d("AttentionManager", "BLOCKED: $packageName | no tokens left")
            false
        }
    }

    private fun startForegroundImmediately() {
        val channelId = "attention_manager_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Attention Manager",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Attention Manager Active")
            .setContentText("Managing attention tokens")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    fun getEventDao(): AttentionEventDao = eventDao
}
