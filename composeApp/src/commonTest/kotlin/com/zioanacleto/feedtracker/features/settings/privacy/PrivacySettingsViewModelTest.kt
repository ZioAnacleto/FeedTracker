package com.zioanacleto.feedtracker.features.settings.privacy

import androidx.lifecycle.SavedStateHandle
import com.zioanacleto.feedtracker.components.formatBirthDateForDisplay
import com.zioanacleto.feedtracker.data.repositories.InMemoryTrackingPreferencesRepository
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.Resource
import com.zioanacleto.feedtracker.domain.export.ExportRequiresConnectionException
import com.zioanacleto.feedtracker.domain.export.anonymizeTrackingSessions
import com.zioanacleto.feedtracker.domain.export.formatTrackingSessionsCsv
import com.zioanacleto.feedtracker.domain.export.formatTrackingSessionsJson
import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.TrackingPreferences
import com.zioanacleto.feedtracker.testutil.FakeSessionExportSharer
import com.zioanacleto.feedtracker.testutil.FakeTrackingSessionsRepository
import com.zioanacleto.feedtracker.testutil.runViewModelTest
import com.zioanacleto.feedtracker.testutil.sampleSession
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test

class PrivacySettingsViewModelTest {

    @Test
    fun openingPrivacyClearsCachedExports() = runViewModelTest {
        val sharer = FakeSessionExportSharer()

        viewModel(sessions = emptyList(), sharer = sharer)

        sharer.clearedExports shouldBe 1
    }

    @Test
    fun exportCsvSharesAnonymizedSessionsIncludingPendingData() = runViewModelTest {
        val sessions = listOf(
            sampleSession(id = "local-1", name = "Mario", surname = "Rossi"),
            sampleSession(id = "local-2", name = "Luigi", surname = "Verdi"),
        )
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(sessions = sessions, sharer = sharer)

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SHARE)

        sharer.shared shouldHaveSize 1
        val shared = sharer.shared.single()
        shared.fileName shouldBe SessionExportFormat.CSV.fileName
        shared.mimeType shouldBe SessionExportFormat.CSV.mimeType
        shared.content shouldBe formatTrackingSessionsCsv(anonymizeTrackingSessions(sessions))
        shared.content.shouldNotContain("Mario")
        shared.content.shouldNotContain("Rossi")
        shared.content.shouldNotContain("Luigi")
        shared.content.shouldNotContain("Verdi")
        shared.content.shouldNotContain("local-1")
        viewModel.uiState.value.isExporting shouldBe false
    }

    @Test
    fun exportJsonSharesAnonymizedSessions() = runViewModelTest {
        val sessions = listOf(sampleSession())
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(sessions = sessions, sharer = sharer)

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SHARE)

        val shared = sharer.shared.single()
        shared.fileName shouldBe SessionExportFormat.JSON.fileName
        shared.content shouldBe formatTrackingSessionsJson(anonymizeTrackingSessions(sessions))
        shared.content.shouldNotContain("\"name\"")
        shared.content.shouldNotContain("\"surname\"")
        shared.content.shouldNotContain("\"id\"")
    }

    @Test
    fun exportUsesThePreferredDateFormat() = runViewModelTest {
        val sessions = listOf(sampleSession().copy(birthDate = "15/03/1990"))
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(
            sessions = sessions,
            sharer = sharer,
            dateFormat = DateDisplayFormat.MONTH_DAY_YEAR,
            formatBirthDate = { value, format -> formatBirthDateForDisplay(value, format) },
            formatDateTime = { _, format -> format.name },
        )

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SHARE)

        val content = sharer.shared.single().content
        content.shouldContain("03/15/1990")
        content.shouldContain("MONTH_DAY_YEAR")
        content.shouldNotContain("15/03/1990")
    }

    @Test
    fun saveUsesTheSystemPickerWhenAvailable() = runViewModelTest {
        val sessions = listOf(sampleSession())
        val sharer = FakeSessionExportSharer()
        val viewModel = viewModel(sessions = sessions, sharer = sharer)

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)

        sharer.shared shouldHaveSize 0
        sharer.saved.single().content shouldBe formatTrackingSessionsJson(anonymizeTrackingSessions(sessions))
        viewModel.uiState.value.saveSucceeded shouldBe true
        viewModel.uiState.value.pendingSave.shouldBeNull()
    }

    @Test
    fun saveDoesNotReportSuccessUntilThePlatformFinishes() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(saveDelayMillis = 1_000),
        )

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)
        testScheduler.runCurrent()

        viewModel.uiState.value.isExporting shouldBe true
        viewModel.uiState.value.saveSucceeded shouldBe false

        testScheduler.advanceUntilIdle()

        viewModel.uiState.value.isExporting shouldBe false
        viewModel.uiState.value.saveSucceeded shouldBe true
    }

    @Test
    fun saveShowsErrorWhenTheFileCannotBeWritten() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(saveError = IllegalStateException("Unable to write the export file")),
        )

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)

        viewModel.uiState.value.error shouldBe "Unable to write the export file"
        viewModel.uiState.value.saveSucceeded shouldBe false
        viewModel.uiState.value.isExporting shouldBe false
    }

    @Test
    fun saveDoesNotReportSuccessWhenThePlatformCancels() = runViewModelTest {
        val sharer = FakeSessionExportSharer(saveResult = SessionExportSaveResult.CANCELLED)
        val viewModel = viewModel(sessions = listOf(sampleSession()), sharer = sharer)

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)

        sharer.saved shouldHaveSize 0
        viewModel.uiState.value.saveSucceeded shouldBe false
        viewModel.uiState.value.isExporting shouldBe false
        viewModel.uiState.value.pendingSave.shouldBeNull()
    }

    @Test
    fun saveWaitsForADocumentPickerWhenThePlatformNeedsOne() = runViewModelTest {
        val sessions = listOf(sampleSession())
        val sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true)
        val viewModel = viewModel(sessions = sessions, sharer = sharer)

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SAVE)

        sharer.saved shouldHaveSize 0
        val pending = viewModel.uiState.value.pendingSave.shouldNotBeNull()
        pending.fileName shouldBe SessionExportFormat.CSV.fileName
        pending.content.shouldNotContain("Mario")

        viewModel.onSaveLauncherOpened()

        viewModel.uiState.value.saveLauncherOpen shouldBe true
        viewModel.uiState.value.pendingSave.shouldNotBeNull()

        viewModel.onSaveCompleted()

        viewModel.uiState.value.pendingSave.shouldBeNull()
        viewModel.uiState.value.saveLauncherOpen shouldBe false
        viewModel.uiState.value.saveSucceeded shouldBe true
    }

    @Test
    fun documentPickerRequestSurvivesViewModelRecreation() = runViewModelTest {
        val handle = SavedStateHandle()
        val sessions = listOf(sampleSession())
        val viewModel = viewModel(
            sessions = sessions,
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
            savedStateHandle = handle,
        )

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SAVE)
        viewModel.onSaveLauncherOpened()

        val restored = viewModel(
            sessions = sessions,
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
            savedStateHandle = handle,
        )

        restored.uiState.value.saveLauncherOpen shouldBe true
        restored.uiState.value.pendingSave.shouldNotBeNull().content.shouldNotContain("Mario")
    }

    @Test
    fun cancellingASaveDropsTheRestoredRequest() = runViewModelTest {
        val handle = SavedStateHandle()
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
            savedStateHandle = handle,
        )

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)
        viewModel.onSaveLauncherOpened()
        viewModel.onSaveCancelled()

        val restored = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
            savedStateHandle = handle,
        )

        restored.uiState.value.pendingSave.shouldBeNull()
        restored.uiState.value.saveLauncherOpen shouldBe false
    }

    @Test
    fun completePendingSaveWritesThePendingContent() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
        )
        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)
        viewModel.onSaveLauncherOpened()
        var written: String? = null

        viewModel.completePendingSave { pending ->
            written = pending.content
        }

        written shouldBe formatTrackingSessionsJson(anonymizeTrackingSessions(listOf(sampleSession())))
        viewModel.uiState.value.saveSucceeded shouldBe true
        viewModel.uiState.value.pendingSave.shouldBeNull()
        viewModel.uiState.value.saveLauncherOpen shouldBe false
    }

    @Test
    fun completePendingSaveReportsFailure() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
        )
        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SAVE)

        viewModel.completePendingSave { error("disk full") }

        viewModel.uiState.value.error shouldBe "disk full"
        viewModel.uiState.value.saveSucceeded shouldBe false
        viewModel.uiState.value.pendingSave.shouldBeNull()
        viewModel.uiState.value.saveLauncherOpen shouldBe false
    }

    @Test
    fun completePendingSaveFailsWhenContentIsMissing() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
        )

        viewModel.completePendingSave { error("should not write") }

        viewModel.uiState.value.error shouldBe "Unable to save the export file"
        viewModel.uiState.value.saveSucceeded shouldBe false
    }

    @Test
    fun exportShowsErrorWhenOfflineAndDoesNotShare() = runViewModelTest {
        val sharer = FakeSessionExportSharer()
        val viewModel = PrivacySettingsViewModel(
            FakeTrackingSessionsRepository(exportError = ExportRequiresConnectionException()),
            InMemoryTrackingPreferencesRepository(),
            sharer,
            formatBirthDate = { value, _ -> value },
            formatDateTime = { millis, _ -> millis.toString() },
        )

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SHARE)

        sharer.shared shouldHaveSize 0
        sharer.saved shouldHaveSize 0
        viewModel.uiState.value.error shouldBe "An internet connection is required to export sessions"
        viewModel.uiState.value.isExporting shouldBe false
        viewModel.uiState.value.pendingSave.shouldBeNull()
    }

    @Test
    fun exportShowsErrorWhenSessionsCannotBeLoaded() = runViewModelTest {
        val sharer = FakeSessionExportSharer()
        val viewModel = PrivacySettingsViewModel(
            FakeTrackingSessionsRepository(sessions = flowOf(Resource.Error("offline"))),
            InMemoryTrackingPreferencesRepository(),
            sharer,
            formatBirthDate = { value, _ -> value },
            formatDateTime = { millis, _ -> millis.toString() },
        )

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SHARE)

        sharer.shared shouldHaveSize 0
        viewModel.uiState.value.error shouldBe "offline"
        viewModel.uiState.value.isExporting shouldBe false
    }

    @Test
    fun exportShowsErrorWhenSharingFails() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(shareError = IllegalStateException("share failed")),
        )

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SHARE)

        viewModel.uiState.value.error shouldBe "share failed"
    }

    @Test
    fun exportShowsLoadingWhileShareIsInFlight() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(shareDelayMillis = 1_000),
        )

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SHARE)
        testScheduler.runCurrent()

        viewModel.uiState.value.isExporting shouldBe true
    }

    @Test
    fun exportIgnoresASecondRequestWhileAlreadyExporting() = runViewModelTest {
        val sharer = FakeSessionExportSharer(shareDelayMillis = 1_000)
        val viewModel = viewModel(sessions = listOf(sampleSession()), sharer = sharer)

        viewModel.exportSessions(SessionExportFormat.CSV, SessionExportDestination.SHARE)
        testScheduler.runCurrent()
        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SHARE)

        sharer.shared shouldHaveSize 0
        testScheduler.advanceUntilIdle()
        sharer.shared shouldHaveSize 1
        sharer.shared.single().fileName shouldBe SessionExportFormat.CSV.fileName
    }

    @Test
    fun desktopExportUsesASingleSetOfActions() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(showsSeparateSaveActions = false),
        )

        viewModel.uiState.value.showsSeparateSaveActions shouldBe false
        exportDestination(showsSeparateSaveActions = false) shouldBe SessionExportDestination.SAVE
        exportDestination(showsSeparateSaveActions = true) shouldBe SessionExportDestination.SHARE
    }

    @Test
    fun exportSharesSessionsAfterTheRepositoryLeavesLoading() = runViewModelTest {
        val sessions = listOf(sampleSession())
        val sharer = FakeSessionExportSharer()
        val viewModel = PrivacySettingsViewModel(
            FakeTrackingSessionsRepository(sessions = flowOf(Resource.Loading, Resource.Success(sessions))),
            InMemoryTrackingPreferencesRepository(),
            sharer,
            formatBirthDate = { value, _ -> value },
            formatDateTime = { millis, _ -> millis.toString() },
        )

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SHARE)

        sharer.shared.single().content shouldBe formatTrackingSessionsJson(anonymizeTrackingSessions(sessions))
        viewModel.uiState.value.isExporting shouldBe false
        viewModel.uiState.value.error.shouldBeNull()
    }

    @Test
    fun saveFailureWithoutAMessageUsesTheFallback() = runViewModelTest {
        val viewModel = viewModel(
            sessions = listOf(sampleSession()),
            sharer = FakeSessionExportSharer(requiresComposeSaveLauncher = true),
        )

        viewModel.onSaveFailed(IllegalStateException())

        viewModel.uiState.value.error shouldBe "Unable to export sessions"
        viewModel.uiState.value.saveSucceeded shouldBe false
        viewModel.uiState.value.pendingSave.shouldBeNull()
    }

    @Test
    fun saveSucceededMessageShownClearsTheFlag() = runViewModelTest {
        val viewModel = viewModel(sessions = listOf(sampleSession()), sharer = FakeSessionExportSharer())

        viewModel.exportSessions(SessionExportFormat.JSON, SessionExportDestination.SAVE)
        viewModel.uiState.value.saveSucceeded shouldBe true

        viewModel.onSaveSucceededMessageShown()

        viewModel.uiState.value.saveSucceeded shouldBe false
    }

    private fun viewModel(
        sessions: List<TrackingSessionModel>,
        sharer: FakeSessionExportSharer,
        dateFormat: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        formatBirthDate: (String, DateDisplayFormat) -> String = { value, _ -> value },
        formatDateTime: (Long, DateDisplayFormat) -> String = { millis, _ -> millis.toString() },
    ) = PrivacySettingsViewModel(
        FakeTrackingSessionsRepository(sessions = flowOf(Resource.Success(sessions))),
        InMemoryTrackingPreferencesRepository(TrackingPreferences(dateFormat = dateFormat)),
        sharer,
        savedStateHandle = savedStateHandle,
        formatBirthDate = formatBirthDate,
        formatDateTime = formatDateTime,
    )
}
