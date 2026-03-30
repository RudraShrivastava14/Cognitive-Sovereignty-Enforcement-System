package com.example.attentiontokenmanager

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.runBlocking

class NotificationGate : NotificationListenerService() {

    private val ignoredPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.example.attentiontokenmanager",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.coloros.safecenter",
        "com.oplus.safecenter"
    )

    private lateinit var database: AppDatabase
    private lateinit var tokenDao: AppTokenDao
    private lateinit var eventDao: AttentionEventDao

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
        tokenDao = database.appTokenDao()
        eventDao = database.attentionEventDao()
        Log.d("NotificationGate", "Service created, DB initialized")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        if (pkg in ignoredPackages) return

        Log.d("NotificationGate", "Notification received from $pkg")

        // Process directly against DB — no dependency on AttentionManagerService.instance
        val allowed = processNotification(pkg)
        if (!allowed) {
            Log.d("NotificationGate", "BLOCKING notification from $pkg")
            cancelNotification(sbn.key)
        } else {
            Log.d("NotificationGate", "ALLOWING notification from $pkg")
        }
    }

    private fun processNotification(packageName: String): Boolean {
        return runBlocking {
            try {
                val currentWindow = TimeWindow.currentWindowStart()
                var token = tokenDao.getToken(packageName)

                // Only manage apps the user explicitly tracks
                if (token == null) {
                    return@runBlocking true
                }

                // Daily reset if window changed
                if (token.lastUpdated != currentWindow) {
                    token = token.copy(
                        remainingTokens = token.maxTokens,
                        lastUpdated = currentWindow,
                        dailyAdjustmentLog = "Tokens reset for new day."
                    )
                    tokenDao.upsertToken(token)
                    Log.d("NotificationGate", "Tokens reset for $packageName -> ${token.maxTokens}")
                }

                val remaining = token.remainingTokens
                val allowed = remaining > 0
                val remainingAfter = if (allowed) remaining - 1 else 0

                if (allowed) {
                    tokenDao.updateRemaining(packageName, remainingAfter)
                }

                Log.d("NotificationGate", "$packageName: allowed=$allowed remaining=$remainingAfter")

                // Log the event
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
                Log.e("NotificationGate", "Error processing notification", e)
                true // Fail-open
            }
        }
    }
}