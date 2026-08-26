package com.zioanacleto.feedtracker.features.home

import app.cash.turbine.test
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import com.zioanacleto.feedtracker.testutil.sampleSession
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
            awaitItem() shouldBe HomeUiState.Ready(
                listOf(HomeSessionListItem.Single(sessions.first())),
            )
        }
    }

    @Test
    fun loadSessionsSortsNewestFirstAndGroupsByPerson() = runViewModelTest {
        val olderMario = sampleSession(id = "mario-old", name = "Mario", sessionStartTime = 1_000L)
        val luigi = sampleSession(id = "luigi", name = "Luigi", sessionStartTime = 2_000L)
        val newerMario = sampleSession(id = "mario-new", name = "Mario", sessionStartTime = 3_000L)
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(
                sessions = flowOf(Resource.Success(listOf(olderMario, luigi, newerMario))),
            ),
        )

        viewModel.loadSessions()

        viewModel.uiState.value shouldBe HomeUiState.Ready(
            listOf(
                HomeSessionListItem.Group(
                    key = personGroupingKey(newerMario),
                    name = "Mario",
                    surname = "Rossi",
                    birthDate = "01/01/1990",
                    sessions = listOf(newerMario, olderMario),
                    isExpanded = false,
                ),
                HomeSessionListItem.Single(luigi),
            ),
        )
    }

    @Test
    fun toggleGroupExpandsAndCollapsesMatchingPerson() = runViewModelTest {
        val first = sampleSession(id = "mario-1", sessionStartTime = 2_000L)
        val second = sampleSession(id = "mario-2", sessionStartTime = 1_000L)
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(sessions = flowOf(Resource.Success(listOf(first, second)))),
        )
        viewModel.loadSessions()
        val key = personGroupingKey(first)

        viewModel.toggleGroup(key)

        val expanded = viewModel.uiState.value as HomeUiState.Ready
        (expanded.items.single() as HomeSessionListItem.Group).isExpanded shouldBe true

        viewModel.toggleGroup(key)

        val collapsed = viewModel.uiState.value as HomeUiState.Ready
        (collapsed.items.single() as HomeSessionListItem.Group).isExpanded shouldBe false
    }

    @Test
    fun selectSessionOpensDetailAndDismissClearsIt() = runViewModelTest {
        val session = sampleSession()
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(sessions = flowOf(Resource.Success(listOf(session)))),
        )
        viewModel.loadSessions()

        viewModel.selectSession(session)
        (viewModel.uiState.value as HomeUiState.Ready).selectedSession shouldBe session

        viewModel.dismissSessionDetail()
        (viewModel.uiState.value as HomeUiState.Ready).selectedSession shouldBe null
    }

    @Test
    fun deleteSelectedSessionRemovesItFromTheListAndShowsUndoSnackbar() = runViewModelTest {
        val session = sampleSession()
        val other = sampleSession(id = "other", name = "Luigi", sessionStartTime = 500L)
        val repository = FakeTrackingSessionsRepository(
            sessions = flowOf(Resource.Success(listOf(session, other))),
        )
        val viewModel = HomeViewModel(repository)
        viewModel.loadSessions()
        viewModel.selectSession(session)

        viewModel.deleteSelectedSession()

        repository.deletedIds shouldBe emptyList()
        viewModel.uiState.value shouldBe HomeUiState.Ready(
            items = listOf(HomeSessionListItem.Single(other)),
            selectedSession = null,
            undoableDeletedSession = session,
        )
    }

    @Test
    fun deleteSelectedSessionCommitsAfterUndoWindow() = runViewModelTest {
        val session = sampleSession()
        val repository = FakeTrackingSessionsRepository(
            sessions = flowOf(Resource.Success(listOf(session))),
            deleteSuspends = true,
        )
        val viewModel = HomeViewModel(repository)
        viewModel.loadSessions()
        viewModel.selectSession(session)

        viewModel.deleteSelectedSession()
        advanceTimeBy(HomeViewModel.DELETE_UNDO_WINDOW_MS)
        advanceUntilIdle()

        repository.deletedIds shouldBe listOf(session.id)
        viewModel.uiState.value shouldBe HomeUiState.Ready(
            items = emptyList(),
            selectedSession = null,
            undoableDeletedSession = null,
        )
    }

    @Test
    fun undoDeleteRestoresSessionAndSkipsApiDelete() = runViewModelTest {
        val session = sampleSession()
        val repository = FakeTrackingSessionsRepository(
            sessions = flowOf(Resource.Success(listOf(session))),
        )
        val viewModel = HomeViewModel(repository)
        viewModel.loadSessions()
        viewModel.selectSession(session)

        viewModel.deleteSelectedSession()
        viewModel.undoDelete()
        advanceTimeBy(HomeViewModel.DELETE_UNDO_WINDOW_MS)
        advanceUntilIdle()

        repository.deletedIds shouldBe emptyList()
        viewModel.uiState.value shouldBe HomeUiState.Ready(
            items = listOf(HomeSessionListItem.Single(session)),
            selectedSession = null,
            undoableDeletedSession = null,
        )
    }

    @Test
    fun deleteSelectedSessionRestoresSessionIfCommitFails() = runViewModelTest {
        val session = sampleSession()
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(
                sessions = flowOf(Resource.Success(listOf(session))),
                deleteError = IllegalStateException("cannot delete"),
            ),
        )
        viewModel.loadSessions()
        viewModel.selectSession(session)

        viewModel.deleteSelectedSession()
        advanceTimeBy(HomeViewModel.DELETE_UNDO_WINDOW_MS)
        advanceUntilIdle()

        val ready = viewModel.uiState.value as HomeUiState.Ready
        ready.selectedSession shouldBe null
        ready.undoableDeletedSession shouldBe null
        ready.deleteError shouldBe "cannot delete"
        ready.items shouldBe listOf(HomeSessionListItem.Single(session))
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
