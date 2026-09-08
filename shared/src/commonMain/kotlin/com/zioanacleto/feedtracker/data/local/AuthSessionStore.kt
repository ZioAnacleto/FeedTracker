package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.domain.auth.AuthSession
import kotlinx.serialization.json.Json

interface AuthSessionStore {
    fun load(): AuthSession?
    fun save(session: AuthSession)
    fun clear()
}

class InMemoryAuthSessionStore(initial: AuthSession? = null) : AuthSessionStore {
    private var session: AuthSession? = initial

    override fun load(): AuthSession? = session

    override fun save(session: AuthSession) {
        this.session = session
    }

    override fun clear() {
        session = null
    }
}

class JsonAuthSessionStore(
    private val textStore: LocalTextStore,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : AuthSessionStore {
    override fun load(): AuthSession? {
        val text = textStore.read()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { json.decodeFromString(AuthSession.serializer(), text) }.getOrNull()
    }

    override fun save(session: AuthSession) {
        textStore.write(json.encodeToString(AuthSession.serializer(), session))
    }

    override fun clear() {
        textStore.delete()
    }
}

const val AUTH_SESSION_FILE_NAME = "auth_session.json"
