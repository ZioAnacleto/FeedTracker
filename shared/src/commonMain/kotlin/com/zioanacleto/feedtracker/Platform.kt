package com.zioanacleto.feedtracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform