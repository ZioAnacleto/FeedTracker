package com.zioanacleto.feedtracker.features.settings.account

import com.zioanacleto.feedtracker.data.repositories.InMemoryTrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.PersonPrefillMode
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
import com.zioanacleto.feedtracker.testutil.FakeSessionExportSharer
import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionController
import com.zioanacleto.feedtracker.widget.InMemoryActiveTrackingSessionStore
import com.zioanacleto.feedtracker.widget.NoOpActiveTrackingSessionNotifier
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AccountSettingsViewModelTest {

    private val session = AuthSession(
        user = UserModel(
            id = "user-1",
            email = "mario@example.com",
            authMethods = listOf(AuthMethod.EMAIL),
            firstName = "Mario",
            lastName = "Rossi",
        ),
        accessToken = "access-token",
    )

    @Test
    fun logoutClearsSessionAfterServerCall() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository(session)
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(
            repository = repository,
            sessionRepository = sessionRepository,
            sharer = sharer,
        )

        viewModel.logout()

        repository.logoutCalls shouldBe 1
        repository.lastLogoutToken shouldBe "access-token"
        sessionRepository.session.value.shouldBeNull()
        sharer.clearedExports shouldBe 1
        viewModel.uiState.value.isLoggingOut shouldBe false
    }

    @Test
    fun logoutClearsSessionEvenWhenServerCallFails() = runViewModelTest {
        val sessionRepository = FakeAuthSessionRepository(session)
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(
            repository = FakeAuthRepository(logoutError = IllegalStateException("offline")),
            sessionRepository = sessionRepository,
            sharer = sharer,
        )

        viewModel.logout()

        viewModel.uiState.value.isLoggingOut shouldBe false
        sessionRepository.session.value.shouldBeNull()
        sharer.clearedExports shouldBe 1
    }

    @Test
    fun logoutShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = viewModel(
            repository = FakeAuthRepository(logoutDelayMillis = 1_000),
        )

        viewModel.logout()
        testScheduler.runCurrent()

        viewModel.uiState.value.isLoggingOut shouldBe true
    }

    @Test
    fun logoutWithoutSessionDoesNotCallServer() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository()
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(
            repository = repository,
            sessionRepository = sessionRepository,
            sharer = sharer,
        )

        viewModel.logout()

        repository.logoutCalls shouldBe 0
        sessionRepository.session.value.shouldBeNull()
        sharer.clearedExports shouldBe 1
    }

    @Test
    fun deleteAccountRemovesServerDataThenClearsLocalStorage() = runViewModelTest {
        val sessions = FakeTrackingSessionsRepository()
        val preferences = InMemoryTrackingPreferencesRepository(
            TrackingPreferences(
                dateFormat = DateDisplayFormat.MONTH_DAY_YEAR,
                personPrefillMode = PersonPrefillMode.CUSTOM,
                defaultPersonName = "Mario",
            ),
        )
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository(session)
        val activeTracking = InMemoryActiveTrackingSessionStore(initialStartTimeMillis = 50L)
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(
            repository = repository,
            sessionRepository = sessionRepository,
            sessionsRepository = sessions,
            preferences = preferences,
            sharer = sharer,
            activeTrackingStore = activeTracking,
        )

        viewModel.deleteAccount("DELETE", "DELETE")

        repository.deleteAccountCalls shouldBe 1
        repository.lastDeleteAccountToken shouldBe "access-token"
        sessions.discardUnsyncedCalls shouldBe 1
        preferences.preferences.value shouldBe TrackingPreferences.Default
        activeTracking.startTimeMillis.value shouldBe null
        sharer.clearedExports shouldBe 1
        sessionRepository.session.value shouldBe null
    }

    @Test
    fun deleteAccountKeepsTheSessionWhenTheServerCallFails() = runViewModelTest {
        val sessions = FakeTrackingSessionsRepository()
        val sessionRepository = FakeAuthSessionRepository(session)
        val viewModel = viewModel(
            repository = FakeAuthRepository(deleteAccountError = IllegalStateException("offline")),
            sessionRepository = sessionRepository,
            sessionsRepository = sessions,
            stringResource = { "Unable to delete the account" },
        )

        viewModel.deleteAccount("DELETE", "DELETE")

        sessions.discardUnsyncedCalls shouldBe 0
        sessionRepository.session.value.shouldNotBeNull()
        viewModel.uiState.value.isDeletingAccount shouldBe false
        viewModel.uiState.value.error shouldBe "offline"
    }

    @Test
    fun deleteAccountIgnoresAConfirmationThatDoesNotMatch() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = viewModel(repository = repository)

        viewModel.deleteAccount("delete", "DELETE")

        repository.deleteAccountCalls shouldBe 0
        viewModel.uiState.value.isDeletingAccount shouldBe false
    }

    private fun viewModel(
        repository: FakeAuthRepository = FakeAuthRepository(),
        sessionRepository: FakeAuthSessionRepository = FakeAuthSessionRepository(session),
        sessionsRepository: FakeTrackingSessionsRepository = FakeTrackingSessionsRepository(),
        preferences: InMemoryTrackingPreferencesRepository = InMemoryTrackingPreferencesRepository(),
        sharer: FakeSessionExportSharer = FakeSessionExportSharer(),
        activeTrackingStore: InMemoryActiveTrackingSessionStore = InMemoryActiveTrackingSessionStore(),
        stringResource: suspend (org.jetbrains.compose.resources.StringResource) -> String = { error("unexpected string") },
    ) = AccountSettingsViewModel(
        authRepository = repository,
        authSessionRepository = sessionRepository,
        sessionExportSharer = sharer,
        trackingSessionsRepository = sessionsRepository,
        trackingPreferencesRepository = preferences,
        activeTrackingSessionController = ActiveTrackingSessionController(
            store = activeTrackingStore,
            notifier = NoOpActiveTrackingSessionNotifier(),
            clock = { 0L },
        ),
        stringResource = stringResource,
    )
}
