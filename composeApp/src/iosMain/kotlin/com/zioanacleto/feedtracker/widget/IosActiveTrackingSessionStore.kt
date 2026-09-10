package com.zioanacleto.feedtracker.widget

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults

private var iosWidgetReloader: () -> Unit = {}

fun setIosWidgetReloader(reloader: () -> Unit) {
    iosWidgetReloader = reloader
}

@OptIn(ExperimentalForeignApi::class)
class IosActiveTrackingSessionStore : ActiveTrackingSessionStore {
    private val defaults = NSUserDefaults(suiteName = ActiveTrackingSessionDefaults.APP_GROUP_ID)
    private val _startTimeMillis = MutableStateFlow(readFromDefaults())
    override val startTimeMillis: StateFlow<Long?> = _startTimeMillis.asStateFlow()
    private val _person = MutableStateFlow(readPersonFromDefaults())
    override val person: StateFlow<ActiveTrackingPerson> = _person.asStateFlow()

    override fun writeStartTimeMillis(startTimeMillis: Long) {
        defaults.setObject(
            NSNumber(longLong = startTimeMillis),
            forKey = ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY,
        )
        defaults.synchronize()
        _startTimeMillis.value = startTimeMillis
    }

    override fun writePerson(name: String, surname: String) {
        defaults.setObject(name, forKey = ActiveTrackingSessionDefaults.NAME_KEY)
        defaults.setObject(surname, forKey = ActiveTrackingSessionDefaults.SURNAME_KEY)
        defaults.synchronize()
        _person.value = ActiveTrackingPerson(name = name, surname = surname)
    }

    override fun clear() {
        defaults.removeObjectForKey(ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY)
        defaults.removeObjectForKey(ActiveTrackingSessionDefaults.NAME_KEY)
        defaults.removeObjectForKey(ActiveTrackingSessionDefaults.SURNAME_KEY)
        defaults.synchronize()
        _startTimeMillis.value = null
        _person.value = ActiveTrackingPerson()
    }

    private fun readFromDefaults(): Long? {
        val number = defaults.objectForKey(ActiveTrackingSessionDefaults.START_TIME_MILLIS_KEY) as? NSNumber
        return number?.longLongValue
    }

    private fun readPersonFromDefaults(): ActiveTrackingPerson = ActiveTrackingPerson(
        name = defaults.stringForKey(ActiveTrackingSessionDefaults.NAME_KEY).orEmpty(),
        surname = defaults.stringForKey(ActiveTrackingSessionDefaults.SURNAME_KEY).orEmpty(),
    )
}

class IosActiveTrackingSessionNotifier : ActiveTrackingSessionNotifier {
    override fun notifyChanged() {
        iosWidgetReloader()
    }
}
