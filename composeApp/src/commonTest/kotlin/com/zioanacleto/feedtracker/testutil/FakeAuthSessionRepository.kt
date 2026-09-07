package com.zioanacleto.feedtracker.testutil

import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAuthSessionRepository(initial: AuthSession? = null) : AuthSessionRepository {
    private val _session = MutableStateFlow(initial)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()

    override fun setSession(session: AuthSession) {
        _session.value = session
    }

    override fun clearSession() {
        _session.value = null
    }
}
