package com.zioanacleto.feedtracker.network

import com.zioanacleto.feedtracker.SERVER_PORT

expect fun getServerHost(): String

fun getServerBaseUrl(): String = "http://${getServerHost()}:$SERVER_PORT"
