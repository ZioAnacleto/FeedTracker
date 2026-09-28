package com.zioanacleto.feedtracker.features.settings.personal

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
import com.zioanacleto.feedtracker.testutil.FakeSessionExportSharer
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PersonalSettingsViewModelTest {

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
        val viewModel = PersonalSettingsViewModel(repository, sessionRepository, sharer)

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
        val viewModel = PersonalSettingsViewModel(
            FakeAuthRepository(logoutError = IllegalStateException("offline")),
            sessionRepository,
            sharer,
        )

        viewModel.logout()

        viewModel.uiState.value.isLoggingOut shouldBe false
        sessionRepository.session.value.shouldBeNull()
        sharer.clearedExports shouldBe 1
    }

    @Test
    fun logoutShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = PersonalSettingsViewModel(
            FakeAuthRepository(logoutDelayMillis = 1_000),
            FakeAuthSessionRepository(session),
            FakeSessionExportSharer(),
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
        val viewModel = PersonalSettingsViewModel(repository, sessionRepository, sharer)

        viewModel.logout()

        repository.logoutCalls shouldBe 0
        sessionRepository.session.value.shouldBeNull()
        sharer.clearedExports shouldBe 1
    }
}
