package com.example.attentiontokenmanager

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationGate : NotificationListenerService() {

    // Packages to completely ignore — system + our own app
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

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        // Skip system and internal packages
        if (pkg in ignoredPackages) return

        Log.d("NotificationGate", "Notification received from $pkg")

        val manager = AttentionManagerService.instance

        if (manager != null) {
            val allowed = manager.handleNotification(pkg)
            if (!allowed) {
                Log.d("NotificationGate", "Blocking notification from $pkg")
                cancelNotification(sbn.key)
            }
        } else {
            Log.d("NotificationGate", "Service instance is null")
        }
    }
}