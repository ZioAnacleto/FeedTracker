package com.zioanacleto.feedtracker.testutil

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
fun runViewModelTest(testBody: suspend TestScope.() -> Unit) = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    Dispatchers.setMain(dispatcher)
    try {
        testBody()
    } finally {
        Dispatchers.resetMain()
    }
}
