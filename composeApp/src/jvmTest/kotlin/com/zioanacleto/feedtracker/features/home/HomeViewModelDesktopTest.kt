package com.zioanacleto.feedtracker.features.home

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class HomeViewModelDesktopTest {

    @Test
    fun mapsRepositoryErrorWithMockk() = runViewModelTest {
        val repository = mockk<TrackingSessionsRepository>()
        coEvery { repository.getTrackingSessions() } returns flowOf(Resource.Error("desktop offline"))

        val viewModel = HomeViewModel(repository)
        viewModel.loadSessions()

        viewModel.uiState.value shouldBe HomeUiState.Error("desktop offline")
    }

    @Test
    fun mapsRepositorySuccessWithMockk() = runViewModelTest {
        val sessions = listOf(
            TrackingSessionModel(
                id = "desktop-1",
                sessionStartTime = 1L,
                sessionEndTime = 2L,
                name = "Luigi",
                surname = "Verdi",
                birthDate = "02/02/1991",
            ),
        )
        val repository = mockk<TrackingSessionsRepository>()
        coEvery { repository.getTrackingSessions() } returns flowOf(Resource.Success(sessions))

        val viewModel = HomeViewModel(repository)
        viewModel.loadSessions()

        viewModel.uiState.value shouldBe HomeUiState.Ready(
            listOf(HomeSessionListItem.Single(sessions.first())),
        )
    }
}
