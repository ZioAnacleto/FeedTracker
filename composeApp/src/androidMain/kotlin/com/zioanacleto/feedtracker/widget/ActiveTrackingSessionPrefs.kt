package com.zioanacleto.feedtracker.widget

import android.content.Context
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionDefaults.NAME_KEY
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionDefaults.PREFS_NAME
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionDefaults.SURNAME_KEY

internal fun readActiveTrackingStartTimeMillis(context: Context): Long? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (!prefs.contains(START_TIME_MILLIS_KEY)) return null
    return prefs.getLong(START_TIME_MILLIS_KEY, 0L)
}

internal fun readActiveTrackingPerson(context: Context): ActiveTrackingPerson {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return ActiveTrackingPerson(
        name = prefs.getString(NAME_KEY, "").orEmpty(),
        surname = prefs.getString(SURNAME_KEY, "").orEmpty(),
    )
}
