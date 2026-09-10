package com.zioanacleto.feedtracker.widget

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveTrackingPerson(val name: String = "", val surname: String = "") {
    val displayName: String
        get() = listOf(name, surname)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
}

interface ActiveTrackingSessionStore {
    val startTimeMillis: StateFlow<Long?>
    val person: StateFlow<ActiveTrackingPerson>
    fun writeStartTimeMillis(startTimeMillis: Long)
    fun writePerson(name: String, surname: String)
    fun clear()
}

class InMemoryActiveTrackingSessionStore(
    initialStartTimeMillis: Long? = null,
    initialPerson: ActiveTrackingPerson = ActiveTrackingPerson(),
) : ActiveTrackingSessionStore {
    private val _startTimeMillis = MutableStateFlow(initialStartTimeMillis)
    override val startTimeMillis: StateFlow<Long?> = _startTimeMillis.asStateFlow()
    private val _person = MutableStateFlow(initialPerson)
    override val person: StateFlow<ActiveTrackingPerson> = _person.asStateFlow()

    override fun writeStartTimeMillis(startTimeMillis: Long) {
        _startTimeMillis.value = startTimeMillis
    }

    override fun writePerson(name: String, surname: String) {
        _person.value = ActiveTrackingPerson(name = name, surname = surname)
    }

    override fun clear() {
        _startTimeMillis.value = null
        _person.value = ActiveTrackingPerson()
    }
}

interface ActiveTrackingSessionNotifier {
    fun notifyChanged()
}

class NoOpActiveTrackingSessionNotifier : ActiveTrackingSessionNotifier {
    override fun notifyChanged() = Unit
}

class ActiveTrackingSessionController(
    private val store: ActiveTrackingSessionStore,
    private val notifier: ActiveTrackingSessionNotifier,
    private val clock: () -> Long,
) {
    fun readStartTimeMillis(): Long? = store.startTimeMillis.value

    fun startOrResume(): Long {
        val existing = store.startTimeMillis.value
        if (existing != null) return existing
        val startTimeMillis = clock()
        store.writeStartTimeMillis(startTimeMillis)
        notifier.notifyChanged()
        return startTimeMillis
    }

    fun updatePerson(name: String, surname: String) {
        if (store.startTimeMillis.value == null) return
        val current = store.person.value
        if (current.name == name && current.surname == surname) return
        store.writePerson(name, surname)
        notifier.notifyChanged()
    }

    fun clear() {
        if (store.startTimeMillis.value == null) return
        store.clear()
        notifier.notifyChanged()
    }
}
