package com.zioanacleto.feedtracker.widget

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidActiveTrackingSessionStore(context: Context) : ActiveTrackingSessionStore {
    private val prefs = context.applicationContext.getSharedPreferences(
        ActiveTrackingSessionDefaults.PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    private val _startTimeMillis = MutableStateFlow(readFromPrefs())
    override val startTimeMillis: StateFlow<Long?> = _startTimeMillis.asStateFlow()
    private val _person = MutableStateFlow(readPersonFromPrefs())
    override val person: StateFlow<ActiveTrackingPerson> = _person.asStateFlow()

    override fun writeStartTimeMillis(startTimeMillis: Long) {
        prefs.edit { putLong(ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY, startTimeMillis) }
        _startTimeMillis.value = startTimeMillis
    }

    override fun writePerson(name: String, surname: String) {
        prefs.edit {
            putString(ActiveTrackingSessionDefaults.NAME_KEY, name)
                .putString(ActiveTrackingSessionDefaults.SURNAME_KEY, surname)
        }
        _person.value = ActiveTrackingPerson(name = name, surname = surname)
    }

    override fun clear() {
        prefs.edit {
            remove(ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY)
                .remove(ActiveTrackingSessionDefaults.NAME_KEY)
                .remove(ActiveTrackingSessionDefaults.SURNAME_KEY)
        }
        _startTimeMillis.value = null
        _person.value = ActiveTrackingPerson()
    }

    private fun readFromPrefs(): Long? {
        if (!prefs.contains(ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY)) return null
        return prefs.getLong(ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY, 0L)
    }

    private fun readPersonFromPrefs(): ActiveTrackingPerson = ActiveTrackingPerson(
        name = prefs.getString(ActiveTrackingSessionDefaults.NAME_KEY, "").orEmpty(),
        surname = prefs.getString(ActiveTrackingSessionDefaults.SURNAME_KEY, "").orEmpty(),
    )
}

class AndroidActiveTrackingSessionNotifier(private val context: Context) : ActiveTrackingSessionNotifier {
    override fun notifyChanged() {
        val appContext = context.applicationContext
        TrackingSessionWidgetProvider.updateAll(appContext)
        TrackingSessionNotificationController.sync(appContext)
    }
}
