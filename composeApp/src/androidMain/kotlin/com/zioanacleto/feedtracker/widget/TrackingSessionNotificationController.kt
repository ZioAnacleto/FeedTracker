package com.zioanacleto.feedtracker.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.zioanacleto.feedtracker.MainActivity
import com.zioanacleto.feedtracker.R

object TrackingSessionNotificationController {
    const val CHANNEL_ID = "tracking_session_timer"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    fun sync(context: Context) {
        val appContext = context.applicationContext
        ensureChannel(appContext)
        val startTimeMillis = readActiveTrackingStartTimeMillis(appContext)
        val intent = Intent(appContext, TrackingSessionNotificationService::class.java)
        if (startTimeMillis == null) {
            appContext.stopService(intent)
            NotificationManagerCompat.from(appContext).cancel(NOTIFICATION_ID)
            return
        }
        if (!canPostNotifications(appContext)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(appContext, intent)
        } else {
            appContext.startService(intent)
        }
    }

    fun buildNotification(context: Context, startTimeMillis: Long): Notification {
        val openAppIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = FeedTrackerDeepLinks.NEW_TRACKING_URI.toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val chronometerBase = SystemClock.elapsedRealtime() -
            (System.currentTimeMillis() - startTimeMillis)
        val displayName = readActiveTrackingPerson(context).displayName
        val title = displayName.ifEmpty { context.getString(R.string.widget_tracking_label) }
        val content = RemoteViews(context.packageName, R.layout.tracking_session_notification).apply {
            setTextViewText(R.id.notification_label, context.getString(R.string.widget_tracking_label))
            if (displayName.isEmpty()) {
                setViewVisibility(R.id.notification_person, View.GONE)
            } else {
                setViewVisibility(R.id.notification_person, View.VISIBLE)
                setTextViewText(R.id.notification_person, displayName)
            }
            setChronometer(R.id.notification_timer, chronometerBase, null, true)
        }
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_timer)
            .setContentTitle(title)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(startTimeMillis)
            .setUsesChronometer(true)
            .setShowWhen(false)
            .setColor(ContextCompat.getColor(context, R.color.feedtracker_orange_deep))
            .setColorized(true)
            .setCustomContentView(content)
            .setCustomBigContentView(content)
            .setCustomHeadsUpContentView(content)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
