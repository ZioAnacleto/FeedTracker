package com.zioanacleto.feedtracker.widget

import io.kotest.matchers.shouldBe
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class NewTrackingNavigatorTest {

    @BeforeTest
    fun resetNavigator() {
        NewTrackingNavigator.consume()
    }

    @AfterTest
    fun consumeOpenRequest() {
        NewTrackingNavigator.consume()
    }

    @Test
    fun requestOpenSetsFlagUntilConsumed() {
        NewTrackingNavigator.openRequested.value shouldBe false
        NewTrackingNavigator.requestOpen()
        NewTrackingNavigator.openRequested.value shouldBe true
        NewTrackingNavigator.requestOpen()
        NewTrackingNavigator.openRequested.value shouldBe true
        NewTrackingNavigator.consume()
        NewTrackingNavigator.openRequested.value shouldBe false
    }
}
