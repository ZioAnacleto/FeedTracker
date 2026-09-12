package com.zioanacleto.feedtracker.features.login

import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ForgotPasswordViewModelTest {

    @Test
    fun sendResetCodeMovesToCodeStepOnSuccess() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()

        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Code
        viewModel.uiState.value.isSubmitting shouldBe false
        repository.startPasswordResetCalls shouldBe 1
        repository.lastStartedEmail shouldBe "mario@example.com"
    }

    @Test
    fun sendResetCodeEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(
            FakeAuthRepository(startPasswordResetError = IllegalStateException("unable to send")),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()

        viewModel.uiState.value.error shouldBe "unable to send"
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Email
    }

    @Test
    fun sendResetCodeShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(
            FakeAuthRepository(startPasswordResetDelayMillis = 1_000),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        testScheduler.runCurrent()

        viewModel.uiState.value.isSubmitting shouldBe true
    }

    @Test
    fun sendResetCodeIgnoresBlankEmail() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository, FakeAuthSessionRepository())

        viewModel.sendResetCode()

        repository.startPasswordResetCalls shouldBe 0
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Email
    }

    @Test
    fun onCodeChangeKeepsOnlyDigitsUpToSix() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(FakeAuthRepository(), FakeAuthSessionRepository())

        viewModel.onCodeChange("12ab34cd56789")

        viewModel.uiState.value.code shouldBe "123456"
    }

    @Test
    fun verifyCodeMovesToPasswordStepOnSuccess() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()

        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Password
        viewModel.uiState.value.resetToken shouldBe "reset-token"
        repository.verifyPasswordResetCalls shouldBe 1
        repository.lastVerifiedCode shouldBe "123456"
    }

    @Test
    fun verifyCodeIgnoresIncompleteCode() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123")
        viewModel.verifyCode()

        repository.verifyPasswordResetCalls shouldBe 0
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Code
    }

    @Test
    fun verifyCodeEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(
            FakeAuthRepository(verifyPasswordResetError = IllegalStateException("invalid code")),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()

        viewModel.uiState.value.error shouldBe "invalid code"
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Code
    }

    @Test
    fun resetPasswordStoresSession() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository()
        val viewModel = ForgotPasswordViewModel(repository, sessionRepository)

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()
        viewModel.onPasswordChange("password2")
        viewModel.resetPassword()

        repository.resetPasswordCalls shouldBe 1
        sessionRepository.session.value?.accessToken shouldBe "access-token"
    }

    @Test
    fun resetPasswordIgnoresBlankPassword() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()
        viewModel.resetPassword()

        repository.resetPasswordCalls shouldBe 0
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Password
    }

    @Test
    fun resetPasswordEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(
            FakeAuthRepository(resetPasswordError = IllegalStateException("unable to reset")),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()
        viewModel.onPasswordChange("password2")
        viewModel.resetPassword()

        viewModel.uiState.value.error shouldBe "unable to reset"
        viewModel.uiState.value.isSubmitting shouldBe false
    }

    @Test
    fun goBackFromCodeReturnsToEmailStep() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(FakeAuthRepository(), FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")

        viewModel.goBack() shouldBe true
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Email
        viewModel.uiState.value.code shouldBe ""
    }

    @Test
    fun goBackFromPasswordReturnsToCodeStep() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(FakeAuthRepository(), FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendResetCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()

        viewModel.goBack() shouldBe true
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Code
    }

    @Test
    fun goBackFromEmailLeavesTheScreen() = runViewModelTest {
        val viewModel = ForgotPasswordViewModel(FakeAuthRepository(), FakeAuthSessionRepository())

        viewModel.goBack() shouldBe false
        viewModel.uiState.value.step shouldBe ForgotPasswordStep.Email
    }
}
