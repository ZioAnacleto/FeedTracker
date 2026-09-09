package com.zioanacleto.feedtracker.features.settings.personal

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
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
        val viewModel = PersonalSettingsViewModel(repository, sessionRepository)

        viewModel.logout()

        repository.logoutCalls shouldBe 1
        repository.lastLogoutToken shouldBe "access-token"
        sessionRepository.session.value.shouldBeNull()
        viewModel.uiState.value.isLoggingOut shouldBe false
    }

    @Test
    fun logoutClearsSessionEvenWhenServerCallFails() = runViewModelTest {
        val sessionRepository = FakeAuthSessionRepository(session)
        val viewModel = PersonalSettingsViewModel(
            FakeAuthRepository(logoutError = IllegalStateException("offline")),
            sessionRepository,
        )

        viewModel.logout()

        viewModel.uiState.value.isLoggingOut shouldBe false
        sessionRepository.session.value.shouldBeNull()
    }

    @Test
    fun logoutShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = PersonalSettingsViewModel(
            FakeAuthRepository(logoutDelayMillis = 1_000),
            FakeAuthSessionRepository(session),
        )

        viewModel.logout()
        testScheduler.runCurrent()

        viewModel.uiState.value.isLoggingOut shouldBe true
    }

    @Test
    fun logoutWithoutSessionDoesNotCallServer() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository()
        val viewModel = PersonalSettingsViewModel(repository, sessionRepository)

        viewModel.logout()

        repository.logoutCalls shouldBe 0
        sessionRepository.session.value.shouldBeNull()
    }
}
