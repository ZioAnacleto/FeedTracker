package com.zioanacleto.feedtracker.features.settings

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
    fun loadsProfileFieldsFromTheCurrentSession() = runViewModelTest {
        val viewModel = PersonalSettingsViewModel(
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
        val viewModel = PersonalSettingsViewModel(repository, sessionRepository)

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
        viewModel.uiState.value.firstName shouldBe "Luigi"
    }

    @Test
    fun saveProfileIgnoresBlankNames() = runViewModelTest {
        val repository = FakeAuthRepository()
        val viewModel = PersonalSettingsViewModel(repository, FakeAuthSessionRepository(session))

        viewModel.onFirstNameChange("  ")
        viewModel.saveProfile()

        repository.updateProfileCalls shouldBe 0
    }

    @Test
    fun saveProfileShowsErrorWhenRequestFails() = runViewModelTest {
        val viewModel = PersonalSettingsViewModel(
            FakeAuthRepository(updateProfileError = IllegalStateException("offline")),
            FakeAuthSessionRepository(session),
        )

        viewModel.onFirstNameChange("Luigi")
        viewModel.saveProfile()

        viewModel.uiState.value.error shouldBe "offline"
        viewModel.uiState.value.isSaving shouldBe false
    }

    @Test
    fun saveProfileShowsLoadingWhileRequestIsInFlight() = runViewModelTest {
        val viewModel = PersonalSettingsViewModel(
            FakeAuthRepository(updateProfileDelayMillis = 1_000),
            FakeAuthSessionRepository(session),
        )

        viewModel.saveProfile()
        testScheduler.runCurrent()

        viewModel.uiState.value.isSaving shouldBe true
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
