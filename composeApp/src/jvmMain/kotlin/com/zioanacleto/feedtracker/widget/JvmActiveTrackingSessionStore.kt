package com.zioanacleto.feedtracker.widget

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class JvmActiveTrackingSessionStore(private val file: File) : ActiveTrackingSessionStore {
    private val _startTimeMillis = MutableStateFlow(readFromFile()?.startTimeMillis)
    override val startTimeMillis: StateFlow<Long?> = _startTimeMillis.asStateFlow()
    private val _person = MutableStateFlow(readFromFile()?.person ?: ActiveTrackingPerson())
    override val person: StateFlow<ActiveTrackingPerson> = _person.asStateFlow()

    override fun writeStartTimeMillis(startTimeMillis: Long) {
        persist(startTimeMillis, _person.value)
        _startTimeMillis.value = startTimeMillis
    }

    override fun writePerson(name: String, surname: String) {
        val person = ActiveTrackingPerson(name = name, surname = surname)
        persist(_startTimeMillis.value, person)
        _person.value = person
    }

    override fun clear() {
        if (file.exists()) {
            file.delete()
        }
        _startTimeMillis.value = null
        _person.value = ActiveTrackingPerson()
    }

    private fun persist(startTimeMillis: Long?, person: ActiveTrackingPerson) {
        val start = startTimeMillis ?: return
        file.parentFile?.mkdirs()
        file.writeText("$start\n${person.name}\n${person.surname}")
    }

    private data class StoredSession(val startTimeMillis: Long, val person: ActiveTrackingPerson)

    private fun readFromFile(): StoredSession? {
        if (!file.exists()) return null
        val lines = file.readText().split('\n')
        val startTimeMillis = lines.firstOrNull()?.trim()?.toLongOrNull() ?: return null
        return StoredSession(
            startTimeMillis = startTimeMillis,
            person = ActiveTrackingPerson(
                name = lines.getOrNull(1).orEmpty(),
                surname = lines.getOrNull(2).orEmpty(),
            ),
        )
    }
}
