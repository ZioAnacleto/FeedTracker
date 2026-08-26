package com.zioanacleto.feedtracker.features.home

import com.zioanacleto.feedtracker.testutil.sampleSession
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HomeSessionListItemTest {

    @Test
    fun keepsSingleSessionsUngroupedAndSortsNewestFirst() {
        val older = sampleSession(id = "older", name = "Luigi", sessionStartTime = 1_000L)
        val newer = sampleSession(id = "newer", name = "Mario", sessionStartTime = 2_000L)

        buildHomeSessionListItems(listOf(older, newer)) shouldBe listOf(
            HomeSessionListItem.Single(newer),
            HomeSessionListItem.Single(older),
        )
    }

    @Test
    fun groupsSamePersonIgnoringNameCaseAndTrim() {
        val first = sampleSession(
            id = "a",
            name = "Mario",
            surname = "Rossi",
            sessionStartTime = 3_000L,
        )
        val second = sampleSession(
            id = "b",
            name = " mario ",
            surname = "ROSSI",
            sessionStartTime = 1_000L,
        )
        val other = sampleSession(
            id = "c",
            name = "Mario",
            surname = "Verdi",
            sessionStartTime = 2_000L,
        )

        buildHomeSessionListItems(listOf(first, second, other)) shouldBe listOf(
            HomeSessionListItem.Group(
                key = personGroupingKey(first),
                name = "Mario",
                surname = "Rossi",
                birthDate = "01/01/1990",
                sessions = listOf(first, second),
                isExpanded = false,
            ),
            HomeSessionListItem.Single(other),
        )
    }

    @Test
    fun marksMatchingGroupsAsExpanded() {
        val first = sampleSession(id = "a", sessionStartTime = 2_000L)
        val second = sampleSession(id = "b", sessionStartTime = 1_000L)
        val key = personGroupingKey(first)

        val items = buildHomeSessionListItems(listOf(first, second), expandedGroupKeys = setOf(key))

        (items.single() as HomeSessionListItem.Group).isExpanded shouldBe true
    }
}
