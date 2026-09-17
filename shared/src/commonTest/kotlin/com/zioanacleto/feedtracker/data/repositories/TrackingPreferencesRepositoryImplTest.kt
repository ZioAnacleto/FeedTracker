package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.local.InMemoryTrackingPreferencesStore
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import com.zioanacleto.feedtracker.domain.repositories.AuthSessionRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TrackingPreferencesRepositoryImplTest {

    @Test
    fun saveWritesLocallyAndSyncsToServer() = runTest {
        val store = InMemoryTrackingPreferencesStore()
        val auth = FakeRemoteAuthRepository()
        val sessions = FakeSessionRepository(
            AuthSession(
                user = UserModel("user-1", "mario@example.com", listOf(AuthMethod.EMAIL), "Mario", "Rossi"),
                accessToken = "access-token",
            ),
        )
        val repository = TrackingPreferencesRepositoryImpl(store, auth, sessions)
        val updated = TrackingPreferences.Default.copy(
            dateFormat = DateDisplayFormat.MONTH_DAY_YEAR,
            dayStartHour = 6,
        )

        repository.save(updated)

        repository.preferences.value shouldBe updated
        store.load() shouldBe updated
        auth.updated shouldBe updated
    }

    @Test
    fun refreshFromRemoteOverwritesLocalCache() = runTest {
        val remote = TrackingPreferences.Default.copy(dayStartHour = 4)
        val store = InMemoryTrackingPreferencesStore(TrackingPreferences.Default)
        val auth = FakeRemoteAuthRepository(remote)
        val sessions = FakeSessionRepository(
            AuthSession(
                user = UserModel("user-1", "mario@example.com", listOf(AuthMethod.EMAIL), "Mario", "Rossi"),
                accessToken = "access-token",
            ),
        )
        val repository = TrackingPreferencesRepositoryImpl(store, auth, sessions)

        repository.refreshFromRemote()

        repository.preferences.value shouldBe remote
        store.load() shouldBe remote
    }

    @Test
    fun rememberLastUsedPersonKeepsOtherSettings() = runTest {
        val store = InMemoryTrackingPreferencesStore(TrackingPreferences.Default.copy(dayStartHour = 5))
        val auth = FakeRemoteAuthRepository()
        val sessions = FakeSessionRepository()
        val repository = TrackingPreferencesRepositoryImpl(store, auth, sessions)

        repository.rememberLastUsedPerson("Luigi", "Bianchi", "02/02/1991")

        repository.preferences.value.dayStartHour shouldBe 5
        repository.preferences.value.lastUsedPersonName shouldBe "Luigi"
        auth.updated shouldBe null
    }
}

private class FakeSessionRepository(initial: AuthSession? = null) : AuthSessionRepository {
    private val _session = MutableStateFlow(initial)
    override val session: StateFlow<AuthSession?> = _session.asStateFlow()
    override fun setSession(session: AuthSession) {
        _session.value = session
    }
    override fun clearSession() {
        _session.value = null
    }
}

private class FakeRemoteAuthRepository(private val remote: TrackingPreferences = TrackingPreferences.Default) : AuthRepository {
    var updated: TrackingPreferences? = null
        private set

    override suspend fun getAvailableAuthMethods() = error("unused")
    override suspend fun startEmailRegistration(email: String) = error("unused")
    override suspend fun verifyEmailCode(email: String, code: String) = error("unused")
    override suspend fun completeEmailRegistration(registrationToken: String, password: String, firstName: String, lastName: String) =
        error("unused")
    override suspend fun loginWithEmail(email: String, password: String) = error("unused")
    override suspend fun startPasswordReset(email: String) = error("unused")
    override suspend fun verifyPasswordResetCode(email: String, code: String) = error("unused")
    override suspend fun resetPassword(resetToken: String, password: String) = error("unused")
    override suspend fun updateProfile(accessToken: String, firstName: String, lastName: String) = error("unused")
    override suspend fun getTrackingPreferences(accessToken: String) = remote
    override suspend fun updateTrackingPreferences(accessToken: String, preferences: TrackingPreferences): TrackingPreferences {
        updated = preferences
        return preferences
    }
    override suspend fun logout(accessToken: String) = error("unused")
}
