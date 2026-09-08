package com.zioanacleto.feedtracker.features.home

import app.cash.turbine.test
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import com.zioanacleto.feedtracker.testutil.sampleSession
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableSharedFlow
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
                items = listOf(HomeSessionListItem.Single(sessions.first())),
                recentSessions = sessions,
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
            items = listOf(
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
            recentSessions = listOf(newerMario, luigi, olderMario),
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
            recentSessions = listOf(other),
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
            recentSessions = emptyList(),
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
            recentSessions = listOf(session),
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
    fun loadSessionsExposesLastSessionAndTodayStats() = runViewModelTest {
        val startOfToday = 1_700_000_000_000L
        val now = startOfToday + 10 * 60 * 60 * 1000L
        val today = sampleSession(
            id = "today",
            sessionStartTime = startOfToday + 60_000L,
            sessionEndTime = startOfToday + 180_000L,
        )
        val older = sampleSession(
            id = "older",
            name = "Luigi",
            sessionStartTime = startOfToday - (8 * 24 * 60 * 60 * 1000L),
            sessionEndTime = startOfToday - (8 * 24 * 60 * 60 * 1000L) + 30_000L,
        )
        val viewModel = HomeViewModel(
            trackingSessionsRepository = FakeTrackingSessionsRepository(
                sessions = flowOf(Resource.Success(listOf(today, older))),
            ),
            clock = { now },
            startOfLocalDay = { startOfToday },
        )

        viewModel.loadSessions()

        viewModel.uiState.value shouldBe HomeUiState.Ready(
            items = listOf(
                HomeSessionListItem.Single(today),
                HomeSessionListItem.Single(older),
            ),
            recentSessions = listOf(today, older),
            stats = HomeStats(
                sessionsToday = 1,
                durationTodayMs = 120_000L,
                sessionsLast7Days = 1,
                averageDurationLast7DaysMs = 120_000L,
            ),
        )
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

    @Test
    fun loadSessionsShowsSyncedCountThenClearsIt() = runViewModelTest {
        val synced = MutableSharedFlow<Int>(extraBufferCapacity = 1)
        val sessions = listOf(sampleSession())
        val viewModel = HomeViewModel(
            FakeTrackingSessionsRepository(
                sessions = flowOf(Resource.Success(sessions)),
                syncedPendingCount = synced,
            ),
        )
        viewModel.loadSessions()
        advanceUntilIdle()

        synced.tryEmit(1)

        (viewModel.uiState.value as HomeUiState.Ready).syncedSessionCount shouldBe 1

        advanceTimeBy(HomeViewModel.SYNC_MESSAGE_WINDOW_MS)
        advanceUntilIdle()

        (viewModel.uiState.value as HomeUiState.Ready).syncedSessionCount shouldBe null
    }
}
