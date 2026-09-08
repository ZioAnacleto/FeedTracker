package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.local.AuthSessionStore
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSessionRepositoryImpl(private val store: AuthSessionStore) : AuthSessionRepository {
    private val _session = MutableStateFlow(store.load())
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    override fun setSession(session: AuthSession) {
        store.save(session)
        _session.value = session
    }

    override fun clearSession() {
        store.clear()
        _session.value = null
    }
}
