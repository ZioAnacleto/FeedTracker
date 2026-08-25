package com.zioanacleto.feedtracker

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@OptIn(ExperimentalTime::class)
fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()