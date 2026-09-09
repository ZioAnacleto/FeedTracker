package com.zioanacleto.feedtracker.features.settings.profile

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.testutil.FakeAuthRepository
import com.zioanacleto.feedtracker.testutil.FakeAuthSessionRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ProfileSettingsViewModelTest {

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
    fun loadsProfileFieldsFromTheCurrentSession() = runViewModelTest {
        val viewModel = ProfileSettingsViewModel(
            FakeAuthRepository(),
            FakeAuthSessionRepository(session),
        )

        viewModel.uiState.value.email shouldBe "mario@example.com"
        viewModel.uiState.value.firstName shouldBe "Mario"
        viewModel.uiState.value.lastName shouldBe "Rossi"
    }

    @Test
    fun saveProfileUpdatesSessionAndKeepsAccessToken() = runViewModelTest {
        val repository = FakeAuthRepository()
        val sessionRepository = FakeAuthSessionRepository(session)
        val viewModel = ProfileSettingsViewModel(repository, sessionRepository)

        viewModel.onFirstNameChange("Luigi")
        viewModel.onLastNameChange("Bianchi")
        viewModel.saveProfile()

        repository.updateProfileCalls shouldBe 1
        repository.lastUpdateToken shouldBe "access-token"
        repository.lastUpdatedFirstName shouldBe "Luigi"
        repository.lastUpdatedLastName shouldBe "Bianchi"
        sessionRepository.session.value?.user?.firstName shouldBe "Luigi"
        sessionRepository.session.value?.user?.lastName shouldBe "Bianchi"
        sessionRepository.session.value?.accessToken shouldBe "access-token"
        viewModel.uiState.value.isSaving shouldBe false
        viewModel.uiState.value.saveSucceeded shouldBe true
        viewModel.uiState.value.firstName shouldBe "Luigi"
    }

    @Test
    fun saveProfileIgnoresBlankNames() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = ProfileSettingsViewModel(repository, FakeAuthSessionRepository(session))

        viewModel.onFirstNameChange("  ")
        viewModel.saveProfile()

        repository.updateProfileCalls shouldBe 0
        viewModel.uiState.value.saveSucceeded shouldBe false
    }

    @Test
    fun saveProfileShowsErrorWhenRequestFails() = runViewModelTest {
        val viewModel = ProfileSettingsViewModel(
            FakeAuthRepository(updateProfileError = IllegalStateException("offline")),
            FakeAuthSessionRepository(session),
        )

        viewModel.onFirstNameChange("Luigi")
        viewModel.saveProfile()

        viewModel.uiState.value.error shouldBe "offline"
        viewModel.uiState.value.isSaving shouldBe false
        viewModel.uiState.value.saveSucceeded shouldBe false
    }

    @Test
    fun saveProfileShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = ProfileSettingsViewModel(
            FakeAuthRepository(updateProfileDelayMillis = 1_000),
            FakeAuthSessionRepository(session),
        )

        viewModel.saveProfile()
        testScheduler.runCurrent()

        viewModel.uiState.value.isSaving shouldBe true
        viewModel.uiState.value.saveSucceeded shouldBe false
    }
}
