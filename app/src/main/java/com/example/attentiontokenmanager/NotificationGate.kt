package com.example.attentiontokenmanager

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationGate : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val pkg = sbn.packageName

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