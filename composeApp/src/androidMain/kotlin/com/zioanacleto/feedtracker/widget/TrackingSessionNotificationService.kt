package com.zioanacleto.feedtracker.widget

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat

class TrackingSessionNotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        TrackingSessionNotificationController.ensureChannel(this)
        val startTimeMillis = readActiveTrackingStartTimeMillis(this)
        if (startTimeMillis == null ||
            !TrackingSessionNotificationController.canPostNotifications(this)
        ) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        val notification = TrackingSessionNotificationController.buildNotification(this, startTimeMillis)
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            TrackingSessionNotificationController.NOTIFICATION_ID,
            notification,
            serviceType,
        )
        return START_STICKY
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
