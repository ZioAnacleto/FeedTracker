package com.zioanacleto.feedtracker

import io.kotest.matchers.string.shouldContain
import kotlin.test.Test

class GreetingTest {

    @Test
    fun greetIncludesPlatformName() {
        Greeting().greet() shouldContain getPlatform().name
    }
}
