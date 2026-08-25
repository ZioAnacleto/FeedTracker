package com.zioanacleto.feedtracker.features.home

import app.cash.turbine.test
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import com.zioanacleto.feedtracker.testutil.sampleSession
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class HomeViewModelTest {

    @Test
    fun loadSessionsExposesReadyState() = runViewModelTest {
        val sessions = listOf(sampleSession())
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(sessions = flowOf(Resource.Success(sessions))),
        )

        viewModel.uiState.test {
            awaitItem() shouldBe HomeUiState.Loading
            viewModel.loadSessions()
            awaitItem() shouldBe HomeUiState.Ready(sessions)
        }
    }

    @Test
    fun loadSessionsExposesErrorState() = runViewModelTest {
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(sessions = flowOf(Resource.Error("boom"))),
        )

        viewModel.uiState.test {
            awaitItem() shouldBe HomeUiState.Loading
            viewModel.loadSessions()
            awaitItem() shouldBe HomeUiState.Error("boom")
        }
    }
}
