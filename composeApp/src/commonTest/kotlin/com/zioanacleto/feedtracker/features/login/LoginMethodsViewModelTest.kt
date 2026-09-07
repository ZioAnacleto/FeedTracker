package com.zioanacleto.feedtracker.features.login

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LoginMethodsViewModelTest {

    @Test
    fun loadMethodsEmitsReadyWithServerMethods() = runViewModelTest {
        val viewModel = LoginMethodsViewModel(
            FakeAuthRepository(methods = listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)),
        )

        viewModel.uiState.value shouldBe LoginMethodsUiState.Ready(
            methods = listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE),
        )
    }

    @Test
    fun loadMethodsEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = LoginMethodsViewModel(
            FakeAuthRepository(methodsError = IllegalStateException("offline")),
        )

        viewModel.uiState.value shouldBe LoginMethodsUiState.Error("offline")
    }

    @Test
    fun unsupportedMethodFlagsReadyState() = runViewModelTest {
        val viewModel = LoginMethodsViewModel(FakeAuthRepository())

        viewModel.onUnsupportedMethod()

        viewModel.uiState.value shouldBe LoginMethodsUiState.Ready(
            methods = listOf(AuthMethod.EMAIL),
            showUnsupportedMethod = true,
        )
    }
}
