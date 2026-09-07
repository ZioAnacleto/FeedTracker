package com.zioanacleto.feedtracker.features.login

import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmailLoginViewModelTest {

    @Test
    fun loginEmitsLoggedInOnSuccess() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository()
        val viewModel = EmailLoginViewModel(repository, sessionRepository)

        viewModel.onEmailChange("mario@example.com")
        viewModel.onPasswordChange("password1")
        viewModel.login()

        viewModel.uiState.value.loggedIn shouldBe true
        viewModel.uiState.value.isLoggingIn shouldBe false
        repository.loginCalls shouldBe 1
        sessionRepository.session.value?.accessToken shouldBe "access-token"
    }

    @Test
    fun loginEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = EmailLoginViewModel(
            FakeAuthRepository(loginError = IllegalStateException("invalid credentials")),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.onPasswordChange("wrong")
        viewModel.login()

        viewModel.uiState.value.error shouldBe "invalid credentials"
        viewModel.uiState.value.loggedIn shouldBe false
        viewModel.uiState.value.isLoggingIn shouldBe false
    }

    @Test
    fun loginShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = EmailLoginViewModel(
            FakeAuthRepository(loginDelayMillis = 1_000),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.onPasswordChange("password1")
        viewModel.login()
        testScheduler.runCurrent()

        viewModel.uiState.value.isLoggingIn shouldBe true
    }

    @Test
    fun loginIgnoresBlankCredentials() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = EmailLoginViewModel(repository, FakeAuthSessionRepository())

        viewModel.login()

        repository.loginCalls shouldBe 0
        viewModel.uiState.value.loggedIn shouldBe false
    }
}
