package com.example.attentiontokenmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AttentionManagerService : Service() {

    private val tokenMap = mutableMapOf<String, Int>()
    private val DEFAULT_DAILY_TOKENS = 5

    private val maxTokenMap = mutableMapOf<String, Int>()

    companion object {
        var instance: AttentionManagerService? = null
    }


    override fun onCreate() {
        super.onCreate()
        instance = this
        startForegroundImmediately()
    }

    private fun getRemainingTokens(pkg: String): Int {
        if (!tokenMap.containsKey(pkg)) {
            val max = maxTokenMap[pkg] ?: DEFAULT_DAILY_TOKENS
            maxTokenMap[pkg] = max
            tokenMap[pkg] = max
        }
        return tokenMap[pkg]!!
    }


    private fun startForegroundImmediately() {
        val channelId = "attention_manager_channel"

        // Create notification channel (MANDATORY for API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Attention Manager",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Use NotificationCompat (SAFER)
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Attention Manager Active")
            .setContentText("Managing attention tokens")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // SAFE system icon
            .setOngoing(true)
            .build()

        // MUST be called quickly after service starts
        startForeground(1, notification)
    }

    fun handleNotification(packageName: String): Boolean {
        val remaining = getRemainingTokens(packageName)

        return if (remaining > 0) {
            tokenMap[packageName] = remaining - 1
            android.util.Log.d(
                "AttentionManager",
                "ALLOWED: $packageName | tokens left = ${remaining - 1}"
            )
            true
        } else {
            android.util.Log.d(
                "AttentionManager",
                "BLOCKED: $packageName | no tokens left"
            )
            false
        }
    }

    fun setMaxTokens(pkg: String, max: Int) {
        maxTokenMap[pkg] = max
        tokenMap[pkg] = max   // reset remaining when changed
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
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
