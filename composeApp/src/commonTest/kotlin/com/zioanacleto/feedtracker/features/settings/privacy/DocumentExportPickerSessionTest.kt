package com.zioanacleto.feedtracker.features.settings.privacy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DocumentExportPickerSessionTest {

    @Test
    fun pickingADocumentCompletesTheSave() = runTest {
        val session = DocumentExportPickerSession()

        val result = async {
            awaitDocumentExport(session) { }
        }
        testScheduler.runCurrent()
        session.isOpen shouldBe true
        session.onDocumentsPicked()

        result.await() shouldBe SessionExportSaveResult.SAVED
        session.isOpen shouldBe false
    }

    @Test
    fun dismissingThePickerCancelsTheSave() = runTest {
        val session = DocumentExportPickerSession()

        val result = async {
            awaitDocumentExport(session) { }
        }
        testScheduler.runCurrent()
        session.onCancelled()

        result.await() shouldBe SessionExportSaveResult.CANCELLED
        session.isOpen shouldBe false
    }

    @Test
    fun aLaterPickerEventDoesNotReplaceTheFirstResult() = runTest {
        val session = DocumentExportPickerSession()

        val result = async {
            awaitDocumentExport(session) { }
        }
        testScheduler.runCurrent()
        session.onCancelled()
        session.onDocumentsPicked()

        result.await() shouldBe SessionExportSaveResult.CANCELLED
    }

    @Test
    fun cancellingTheWaitDropsALaterPickerResult() = runTest {
        val session = DocumentExportPickerSession()
        val waiting = launch {
            awaitDocumentExport(session) { }
        }
        testScheduler.runCurrent()

        waiting.cancel()
        testScheduler.advanceUntilIdle()

        session.isOpen shouldBe false
        session.onDocumentsPicked()
        session.onCancelled()
    }

    @Test
    fun aFailedPresentationDoesNotLeaveThePickerOpen() = runTest {
        val session = DocumentExportPickerSession()

        val failure = shouldThrow<IllegalStateException> {
            awaitDocumentExport(session) { error("No root view controller") }
        }

        failure.message shouldBe "No root view controller"
        session.isOpen shouldBe false
    }

    @Test
    fun openingASecondPickerWhileOneIsOpenFails() {
        val session = DocumentExportPickerSession()
        session.open { }

        shouldThrow<IllegalStateException> {
            session.open { }
        }
    }

    @Test
    fun exportWriteFailureUsesThePlatformMessage() {
        exportWriteFailureMessage(written = true, platformMessage = "disk full").shouldBeNull()
        exportWriteFailureMessage(written = false, platformMessage = "disk full") shouldBe "disk full"
        exportWriteFailureMessage(written = false, platformMessage = "  ") shouldBe "Unable to write the export file"
        exportWriteFailureMessage(written = false, platformMessage = null) shouldBe "Unable to write the export file"
    }
}
