package com.zioanacleto.feedtracker.widget

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NewTrackingNavigator {
    private val _openRequested = MutableStateFlow(false)
    val openRequested: StateFlow<Boolean> = _openRequested.asStateFlow()

    fun requestOpen() {
        _openRequested.value = true
    }

    fun consume() {
        _openRequested.value = false
    }
}
