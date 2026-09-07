package com.zioanacleto.feedtracker.features.login

import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class EmailSignUpViewModelTest {

    @Test
    fun sendVerificationCodeMovesToCodeStepOnSuccess() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = EmailSignUpViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()

        viewModel.uiState.value.step shouldBe EmailSignUpStep.Code
        viewModel.uiState.value.isSubmitting shouldBe false
        repository.startRegistrationCalls shouldBe 1
        repository.lastStartedEmail shouldBe "mario@example.com"
    }

    @Test
    fun sendVerificationCodeEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = EmailSignUpViewModel(
            FakeAuthRepository(startRegistrationError = IllegalStateException("already registered")),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()

        viewModel.uiState.value.error shouldBe "already registered"
        viewModel.uiState.value.step shouldBe EmailSignUpStep.Email
        viewModel.uiState.value.isSubmitting shouldBe false
    }

    @Test
    fun sendVerificationCodeShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = EmailSignUpViewModel(
            FakeAuthRepository(startRegistrationDelayMillis = 1_000),
            FakeAuthSessionRepository(),
        )

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()
        testScheduler.runCurrent()

        viewModel.uiState.value.isSubmitting shouldBe true
    }

    @Test
    fun sendVerificationCodeIgnoresBlankEmail() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = EmailSignUpViewModel(repository, FakeAuthSessionRepository())

        viewModel.sendVerificationCode()

        repository.startRegistrationCalls shouldBe 0
        viewModel.uiState.value.step shouldBe EmailSignUpStep.Email
    }

    @Test
    fun verifyCodeMovesToProfileStepOnSuccess() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = EmailSignUpViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()

        viewModel.uiState.value.step shouldBe EmailSignUpStep.Profile
        viewModel.uiState.value.registrationToken shouldBe "reg-token"
        repository.verifyCalls shouldBe 1
        repository.lastVerifiedCode shouldBe "123456"
    }

    @Test
    fun verifyCodeIgnoresIncompleteCode() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = EmailSignUpViewModel(repository, FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()
        viewModel.onCodeChange("123")
        viewModel.verifyCode()

        repository.verifyCalls shouldBe 0
        viewModel.uiState.value.step shouldBe EmailSignUpStep.Code
    }

    @Test
    fun completeRegistrationStoresSession() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository()
        val viewModel = EmailSignUpViewModel(repository, sessionRepository)

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()
        viewModel.onCodeChange("123456")
        viewModel.verifyCode()
        viewModel.onFirstNameChange("Mario")
        viewModel.onLastNameChange("Rossi")
        viewModel.onPasswordChange("password1")
        viewModel.completeRegistration()

        repository.completeCalls shouldBe 1
        sessionRepository.session.value?.accessToken shouldBe "access-token"
    }

    @Test
    fun goBackFromCodeReturnsToEmailStep() = runViewModelTest {
        val viewModel = EmailSignUpViewModel(FakeAuthRepository(), FakeAuthSessionRepository())

        viewModel.onEmailChange("mario@example.com")
        viewModel.sendVerificationCode()
        viewModel.onCodeChange("123456")

        viewModel.goBack() shouldBe true
        viewModel.uiState.value.step shouldBe EmailSignUpStep.Email
        viewModel.uiState.value.code shouldBe ""
    }
}
