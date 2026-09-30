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

    @Test
    fun usesTheAvailableInitialWhenOnlyOneNamePartIsPresent() {
        val exported = anonymizeTrackingSessions(
            listOf(
                session(id = "name", name = "Mario", surname = " "),
                session(id = "surname", name = " ", surname = "Rossi"),
            ),
        )

        exported.map { it.initials } shouldBe listOf("M.", "R.")
    }

    @Test
    fun disambiguatesThreePeopleWhoShareTheSameInitials() {
        val exported = anonymizeTrackingSessions(
            listOf(
                session(id = "mario", name = "Mario", surname = "Rossi"),
                session(id = "maria", name = "Maria", surname = "Rossi"),
                session(id = "marco", name = "Marco", surname = "Rossi"),
            ),
        )

        exported.map { it.initials } shouldBe listOf("M.R.3", "M.R.2", "M.R.")
    }
}

class TrackingSessionExportFormatterTest {

    private val csvBom = "\uFEFF"
    private val csvLineEnding = "\r\n"

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
            csvBom +
            "initials,birthDate,sessionStartTime,sessionEndTime,additionalNotes$csvLineEnding" +
            "M.R.,01/01/1990,01/01/2026 10:00,01/01/2026 10:15,\"said \"\"hello\"\", then napped\"$csvLineEnding"
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
            csvBom + "initials,birthDate,sessionStartTime,sessionEndTime,additionalNotes$csvLineEnding"
        formatTrackingSessionsJson(emptyList()) shouldBe "[]"
    }

    @Test
    fun csvNeutralizesFormulaInjection() {
        val csv = formatTrackingSessionsCsv(
            listOf(
                AnonymizedTrackingSession(
                    initials = "=CMD",
                    birthDate = "+1234",
                    sessionStartTime = "-1",
                    sessionEndTime = "@SUM(A1)",
                    additionalNotes = "\tformula",
                ),
            ),
        )

        csv shouldBe
            csvBom +
            "initials,birthDate,sessionStartTime,sessionEndTime,additionalNotes$csvLineEnding" +
            "'=CMD,'+1234,'-1,'@SUM(A1),'\tformula$csvLineEnding"
    }

    @Test
    fun csvEscapesMultilineNotesAndLeavesNullNotesEmpty() {
        val csv = formatTrackingSessionsCsv(
            listOf(
                AnonymizedTrackingSession(
                    initials = "M.R.",
                    birthDate = "01/01/1990",
                    sessionStartTime = "01/01/2026 10:00",
                    sessionEndTime = "01/01/2026 10:15",
                    additionalNotes = "line1\nline2\rline3",
                ),
                AnonymizedTrackingSession(
                    initials = "A.B.",
                    birthDate = "02/02/1991",
                    sessionStartTime = "02/02/2026 11:00",
                    sessionEndTime = "02/02/2026 11:30",
                    additionalNotes = null,
                ),
            ),
        )

        csv shouldBe
            csvBom +
            "initials,birthDate,sessionStartTime,sessionEndTime,additionalNotes$csvLineEnding" +
            "M.R.,01/01/1990,01/01/2026 10:00,01/01/2026 10:15,\"line1\nline2\rline3\"$csvLineEnding" +
            "A.B.,02/02/1991,02/02/2026 11:00,02/02/2026 11:30,$csvLineEnding"
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
