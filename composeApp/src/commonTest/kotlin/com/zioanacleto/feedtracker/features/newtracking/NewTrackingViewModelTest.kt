package com.zioanacleto.feedtracker.features.newtracking

import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import kotlin.test.Test

class NewTrackingViewModelTest {

    @Test
    fun saveNewTrackingEmitsSavedAndStoresSession() = runViewModelTest {
        val repository = FakeTrackingSessionsRepository()
        val viewModel = NewTrackingViewModel(repository)

        viewModel.saveState.value shouldBe SaveTrackingUiState.Idle
        viewModel.saveNewTracking(
            name = "Mario",
            surname = "Rossi",
            birthDate = "01/01/1990",
            additionalNotes = "  ",
            startTime = 1_000L,
            endTime = 2_000L,
        )

        viewModel.saveState.value shouldBe SaveTrackingUiState.Saved
        val saved = repository.saved.single()
        saved.name shouldBe "Mario"
        saved.surname shouldBe "Rossi"
        saved.birthDate shouldBe "01/01/1990"
        saved.additionalNotes.shouldBeNull()
        saved.sessionStartTime shouldBe 1_000L
        saved.sessionEndTime shouldBe 2_000L
        saved.id.shouldNotBeBlank()
    }

    @Test
    fun saveNewTrackingEmitsErrorWhenRepositoryFails() = runViewModelTest {
        val viewModel = NewTrackingViewModel(
            FakeTrackingSessionsRepository(saveError = IllegalStateException("disk full")),
        )

        viewModel.saveNewTracking(
            name = "Mario",
            surname = "Rossi",
            birthDate = "01/01/1990",
            startTime = 1_000L,
            endTime = 2_000L,
        )

        viewModel.saveState.value shouldBe SaveTrackingUiState.Error("disk full")
    }

    @Test
    fun popupAndSaveStateHelpersUpdateUi() = runViewModelTest {
        val viewModel = NewTrackingViewModel(FakeTrackingSessionsRepository())

        viewModel.showPopup()
        viewModel.showPopup.value shouldBe true
        viewModel.hidePopup()
        viewModel.showPopup.value shouldBe false

        viewModel.saveNewTracking(
            name = "Mario",
            surname = "Rossi",
            birthDate = "01/01/1990",
            startTime = 1_000L,
            endTime = 2_000L,
        )
        viewModel.saveState.value shouldBe SaveTrackingUiState.Saved
        viewModel.consumeSaveState()
        viewModel.saveState.value shouldBe SaveTrackingUiState.Idle
    }
}
