package com.zioanacleto.feedtracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.zioanacleto.feedtracker.MainActivity
import com.zioanacleto.feedtracker.R

class TrackingSessionWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context))
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TrackingSessionWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val views = buildRemoteViews(context)
            ids.forEach { manager.updateAppWidget(it, views) }
        }

        internal fun buildRemoteViews(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.tracking_session_widget)
            val startTimeMillis = readActiveTrackingStartTimeMillis(context)
            val openAppIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    data = FeedTrackerDeepLinks.NEW_TRACKING_URI.toUri()
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent)

            if (startTimeMillis == null) {
                views.setViewVisibility(R.id.widget_timer, View.GONE)
                views.setViewVisibility(R.id.widget_idle_timer, View.VISIBLE)
                views.setViewVisibility(R.id.widget_person, View.GONE)
                views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_idle_label))
            } else {
                val elapsedRealtimeBase = SystemClock.elapsedRealtime() -
                    (System.currentTimeMillis() - startTimeMillis)
                val displayName = readActiveTrackingPerson(context).displayName
                views.setViewVisibility(R.id.widget_timer, View.VISIBLE)
                views.setViewVisibility(R.id.widget_idle_timer, View.GONE)
                views.setChronometer(R.id.widget_timer, elapsedRealtimeBase, null, true)
                views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_tracking_label))
                if (displayName.isEmpty()) {
                    views.setViewVisibility(R.id.widget_person, View.GONE)
                } else {
                    views.setViewVisibility(R.id.widget_person, View.VISIBLE)
                    views.setTextViewText(R.id.widget_person, displayName)
                }
            }
            return views
        }
    }
}
