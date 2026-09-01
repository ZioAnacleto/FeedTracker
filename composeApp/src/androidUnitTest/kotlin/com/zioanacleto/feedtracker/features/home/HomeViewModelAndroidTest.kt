package com.zioanacleto.feedtracker.features.home

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class HomeViewModelAndroidTest {

    @Test
    fun mapsRepositorySuccessWithMockk() = runViewModelTest {
        val sessions = listOf(
            TrackingSessionModel(
                id = "android-1",
                sessionStartTime = 1L,
                sessionEndTime = 2L,
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
            ),
        )
        val repository = mockk<TrackingSessionsRepository>()
        every { repository.syncedPendingCount } returns emptyFlow()
        coEvery { repository.getTrackingSessions() } returns flowOf(Resource.Success(sessions))

        val viewModel = HomeViewModel(repository)
        viewModel.loadSessions()

        viewModel.uiState.value shouldBe HomeUiState.Ready(
            items = listOf(HomeSessionListItem.Single(sessions.first())),
            recentSessions = sessions,
        )
    }
}
