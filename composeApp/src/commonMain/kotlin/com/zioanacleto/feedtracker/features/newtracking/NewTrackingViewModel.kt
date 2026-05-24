package com.zioanacleto.feedtracker.features.newtracking

import androidx.lifecycle.ViewModel
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.domain.repositories.TrackingSessionsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NewTrackingViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val trackingSessionsRepository: TrackingSessionsRepository
) : ViewModel() {

    private val ioCoroutineScope by lazy { CoroutineScope(dispatcherProvider.io()) }

    private val _showPopup = MutableStateFlow(false)
    val showPopup: StateFlow<Boolean> = _showPopup.asStateFlow()

    fun saveNewTracking(
        name: String,
        surname: String,
        birthDate: String,
        additionalNotes: String? = null,
        startTime: Long,
        endTime: Long
    ) {
        val trackingModel = TrackingSessionModel(
            id = Uuid.random().toString(),
            name = name,
            surname = surname,
            birthDate = birthDate,
            additionalNotes = additionalNotes,
            sessionStartTime = startTime,
            sessionEndTime = endTime
        )

        println("trackingModel: $trackingModel")

        ioCoroutineScope.launch {
            trackingSessionsRepository.saveTrackingSession(trackingModel)
        }
    }

    fun showPopup() {
        _showPopup.value = true
    }

    fun hidePopup() {
        _showPopup.value = false
    }
}