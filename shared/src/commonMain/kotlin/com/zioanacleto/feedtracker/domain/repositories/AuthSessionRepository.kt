package com.zioanacleto.feedtracker.domain.repositories

import com.zioanacleto.feedtracker.domain.auth.AuthSession
import kotlinx.coroutines.flow.StateFlow

interface AuthSessionRepository {
    val session: StateFlow<AuthSession?>
    fun setSession(session: AuthSession)
    fun clearSession()
}
