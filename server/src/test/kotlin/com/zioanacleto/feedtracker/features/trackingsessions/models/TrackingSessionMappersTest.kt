package com.zioanacleto.feedtracker.features.trackingsessions.models

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test

class TrackingSessionMappersTest {

    @Test
    fun createRequestToModelCopiesFieldsAndGeneratesId() {
        val request = CreateTrackingSessionRequest(
            sessionStartTime = 1_000L,
            sessionEndTime = 2_000L,
            name = "Mario",
            surname = "Rossi",
            birthDate = "01/01/1990",
            additionalNotes = "note",
        )

        val model = request.toModel()

        model.id.shouldNotBeBlank()
        model.sessionStartTime shouldBe 1_000L
        model.sessionEndTime shouldBe 2_000L
        model.name shouldBe "Mario"
        model.surname shouldBe "Rossi"
        model.birthDate shouldBe "01/01/1990"
        model.additionalNotes shouldBe "note"
    }

    @Test
    fun updateRequestToModelUsesProvidedId() {
        val request = UpdateTrackingSessionRequest(
            sessionStartTime = 1_000L,
            sessionEndTime = 3_000L,
            name = "Luigi",
            surname = "Verdi",
            birthDate = "02/02/1991",
            additionalNotes = null,
        )

        val model = request.toModel("fixed-id")

        model.id shouldBe "fixed-id"
        model.sessionEndTime shouldBe 3_000L
        model.name shouldBe "Luigi"
        model.additionalNotes shouldBe null
    }
}
