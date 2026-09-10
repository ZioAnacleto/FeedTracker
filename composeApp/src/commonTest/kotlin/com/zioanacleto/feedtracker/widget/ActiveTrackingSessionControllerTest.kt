package com.zioanacleto.feedtracker.widget

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ActiveTrackingSessionControllerTest {

    @Test
    fun startOrResumePersistsNewStartTime() {
        val store = InMemoryActiveTrackingSessionStore()
        val notifier = RecordingActiveTrackingSessionNotifier()
        val controller = ActiveTrackingSessionController(
            store = store,
            notifier = notifier,
            clock = { 1_700_000L },
        )

        controller.startOrResume() shouldBe 1_700_000L
        store.startTimeMillis.value shouldBe 1_700_000L
        notifier.count shouldBe 1
        controller.startOrResume() shouldBe 1_700_000L
        notifier.count shouldBe 1
    }

    @Test
    fun clearRemovesPersistedSessionAndPerson() {
        val store = InMemoryActiveTrackingSessionStore(
            initialStartTimeMillis = 42L,
            initialPerson = ActiveTrackingPerson(name = "Mario", surname = "Rossi"),
        )
        val notifier = RecordingActiveTrackingSessionNotifier()
        val controller = ActiveTrackingSessionController(
            store = store,
            notifier = notifier,
            clock = { 99L },
        )

        controller.clear()
        store.startTimeMillis.value shouldBe null
        store.person.value shouldBe ActiveTrackingPerson()
        notifier.count shouldBe 1
        controller.clear()
        notifier.count shouldBe 1
        controller.startOrResume() shouldBe 99L
    }

    @Test
    fun updatePersonPersistsNameAndSurnameForAnActiveSession() {
        val store = InMemoryActiveTrackingSessionStore()
        val notifier = RecordingActiveTrackingSessionNotifier()
        val controller = ActiveTrackingSessionController(
            store = store,
            notifier = notifier,
            clock = { 1L },
        )

        controller.updatePerson(name = "Mario", surname = "Rossi")
        store.person.value shouldBe ActiveTrackingPerson()
        notifier.count shouldBe 0

        controller.startOrResume()
        notifier.count shouldBe 1
        controller.updatePerson(name = "Mario", surname = "Rossi")
        store.person.value shouldBe ActiveTrackingPerson(name = "Mario", surname = "Rossi")
        notifier.count shouldBe 2
        controller.updatePerson(name = "Mario", surname = "Rossi")
        store.person.value shouldBe ActiveTrackingPerson(name = "Mario", surname = "Rossi")
        notifier.count shouldBe 2
    }

    @Test
    fun displayNameJoinsPresentNameParts() {
        ActiveTrackingPerson().displayName shouldBe ""
        ActiveTrackingPerson(name = "  ", surname = "  ").displayName shouldBe ""
        ActiveTrackingPerson(name = "  Mario  ", surname = "").displayName shouldBe "Mario"
        ActiveTrackingPerson(name = "", surname = "Rossi").displayName shouldBe "Rossi"
        ActiveTrackingPerson(name = "Mario", surname = "Rossi").displayName shouldBe "Mario Rossi"
    }
}

private class RecordingActiveTrackingSessionNotifier : ActiveTrackingSessionNotifier {
    var count = 0
    override fun notifyChanged() {
        count += 1
    }
}
