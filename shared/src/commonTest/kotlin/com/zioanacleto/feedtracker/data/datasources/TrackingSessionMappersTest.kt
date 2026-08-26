package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.testutil.trackingSession
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TrackingSessionMappersTest {

    @Test
    fun toCreateRequestCopiesSessionFieldsWithoutId() {
        val session = trackingSession().copy(additionalNotes = "note")

        val request = session.toCreateRequest()

        request.sessionStartTime shouldBe session.sessionStartTime
        request.sessionEndTime shouldBe session.sessionEndTime
        request.name shouldBe session.name
        request.surname shouldBe session.surname
        request.birthDate shouldBe session.birthDate
        request.additionalNotes shouldBe "note"
    }
}
