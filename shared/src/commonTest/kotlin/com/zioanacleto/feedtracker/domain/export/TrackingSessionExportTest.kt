package com.zioanacleto.feedtracker.domain.export

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class TrackingSessionAnonymizerTest {

    @Test
    fun replacesNameAndSurnameWithInitials() {
        val exported = anonymizeTrackingSessions(
            sessions = listOf(session(id = "1", name = "Mario", surname = "Rossi")),
            formatBirthDate = { "born:$it" },
            formatDateTime = { "time:$it" },
        )

        exported shouldBe listOf(
            AnonymizedTrackingSession(
                initials = "M.R.",
                birthDate = "born:01/01/1990",
                sessionStartTime = "time:1000",
                sessionEndTime = "time:2000",
                additionalNotes = "hungry",
            ),
        )
    }

    @Test
    fun keepsTheSameInitialsForTheSamePerson() {
        val exported = anonymizeTrackingSessions(
            listOf(
                session(id = "1", name = "Mario", surname = "Rossi"),
                session(id = "2", name = " mario ", surname = "ROSSI"),
            ),
        )

        exported.map { it.initials } shouldBe listOf("M.R.", "M.R.")
    }

    @Test
    fun disambiguatesPeopleWhoShareTheSameInitials() {
        val exported = anonymizeTrackingSessions(
            listOf(
                session(id = "mario", name = "Mario", surname = "Rossi"),
                session(id = "maria", name = "Maria", surname = "Rossi"),
            ),
        )

        exported.map { it.initials } shouldBe listOf("M.R.2", "M.R.")
    }

    @Test
    fun usesFallbackInitialsWhenTheNameIsMissing() {
        val exported = anonymizeTrackingSessions(
            listOf(session(id = "1", name = "  ", surname = "  ")),
        )

        exported.single().initials shouldBe "?."
    }
}

class TrackingSessionExportFormatterTest {

    @Test
    fun csvIncludesHeadersAndEscapesNotesWithoutSessionIds() {
        val csv = formatTrackingSessionsCsv(
            listOf(
                AnonymizedTrackingSession(
                    initials = "M.R.",
                    birthDate = "01/01/1990",
                    sessionStartTime = "01/01/2026 10:00",
                    sessionEndTime = "01/01/2026 10:15",
                    additionalNotes = "said \"hello\", then napped",
                ),
            ),
        )

        csv.shouldNotContain("session-1")
        csv shouldBe
            "initials,birthDate,sessionStartTime,sessionEndTime,additionalNotes\n" +
            "M.R.,01/01/1990,01/01/2026 10:00,01/01/2026 10:15,\"said \"\"hello\"\", then napped\"\n"
    }

    @Test
    fun jsonIsPrettyPrintedWithoutIdsOrGivenNames() {
        val json = formatTrackingSessionsJson(
            listOf(
                AnonymizedTrackingSession(
                    initials = "M.R.",
                    birthDate = "01/01/1990",
                    sessionStartTime = "01/01/2026 10:00",
                    sessionEndTime = "01/01/2026 10:15",
                    additionalNotes = null,
                ),
            ),
        )

        json.shouldNotContain("Mario")
        json.shouldNotContain("Rossi")
        json.shouldNotContain("\"id\"")
        json.shouldNotContain("\"name\"")
        json.shouldNotContain("\"surname\"")
        json shouldBe """
            [
                {
                    "initials": "M.R.",
                    "birthDate": "01/01/1990",
                    "sessionStartTime": "01/01/2026 10:00",
                    "sessionEndTime": "01/01/2026 10:15",
                    "additionalNotes": null
                }
            ]
        """.trimIndent()
    }

    @Test
    fun emptyExportKeepsAReadableStructure() {
        formatTrackingSessionsCsv(emptyList()) shouldBe
            "initials,birthDate,sessionStartTime,sessionEndTime,additionalNotes\n"
        formatTrackingSessionsJson(emptyList()) shouldBe "[]"
    }
}

private fun session(id: String, name: String, surname: String) = TrackingSessionModel(
    id = id,
    sessionStartTime = 1_000L,
    sessionEndTime = 2_000L,
    name = name,
    surname = surname,
    birthDate = "01/01/1990",
    additionalNotes = "hungry",
)
