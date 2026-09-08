package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

interface LocalTextStore {
    fun read(): String?
    fun write(value: String)
    fun delete()
}

interface PendingSessionsStore {
    fun load(): List<TrackingSessionModel>
    fun save(sessions: List<TrackingSessionModel>)
}

class InMemoryPendingSessionsStore : PendingSessionsStore {
    private val sessions = mutableListOf<TrackingSessionModel>()

    override fun load(): List<TrackingSessionModel> = sessions.toList()

    override fun save(sessions: List<TrackingSessionModel>) {
        this.sessions.clear()
        this.sessions.addAll(sessions)
    }
}

class FileTextStore(private val path: String) : LocalTextStore {
    override fun read(): String? = readTextFile(path)

    override fun write(value: String) = writeTextFile(path, value)

    override fun delete() = deleteTextFile(path)
}

class JsonPendingSessionsStore(
    private val textStore: LocalTextStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : PendingSessionsStore {
    override fun load(): List<TrackingSessionModel> {
        val text = textStore.read()?.takeIf { it.isNotBlank() } ?: return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(TrackingSessionModel.serializer()), text)
        }.getOrDefault(emptyList())
    }

    override fun save(sessions: List<TrackingSessionModel>) {
        if (sessions.isEmpty()) {
            textStore.delete()
        } else {
            textStore.write(json.encodeToString(ListSerializer(TrackingSessionModel.serializer()), sessions))
        }
    }
}

const val PENDING_TRACKING_SESSIONS_FILE_NAME = "pending_tracking_sessions.json"
