package com.example.attentiontokenmanager

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationGate : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        val manager = AttentionManagerService.instance
        if (manager != null) {
            val allowed = manager.handleNotification(pkg)

            if (!allowed) {
                // Block the notification
                cancelNotification(sbn.key)
            }
        }
    }


}
