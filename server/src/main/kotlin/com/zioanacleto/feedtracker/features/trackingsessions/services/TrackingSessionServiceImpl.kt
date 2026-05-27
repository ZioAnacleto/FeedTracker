package com.zioanacleto.feedtracker.features.trackingsessions.services

import com.zioanacleto.feedtracker.common.exceptions.ResourceNotFoundException
import com.zioanacleto.feedtracker.common.exceptions.ValidationException
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import com.zioanacleto.feedtracker.features.trackingsessions.repositories.TrackingSessionRepository

class TrackingSessionServiceImpl(
    private val repository: TrackingSessionRepository,
) : TrackingSessionService {

    override suspend fun getAll(): List<TrackingSessionModel> = repository.findAll()

    override suspend fun getById(id: String): TrackingSessionModel =
        repository.findById(id) ?: throw ResourceNotFoundException("Tracking session", id)

    override suspend fun create(request: CreateTrackingSessionRequest): TrackingSessionModel {
        validateSessionTimes(request.sessionStartTime, request.sessionEndTime)
        validatePersonFields(request.name, request.surname, request.birthDate)
        return repository.create(request)
    }

    override suspend fun update(
        id: String,
        request: UpdateTrackingSessionRequest
    ): TrackingSessionModel {
        validateSessionTimes(request.sessionStartTime, request.sessionEndTime)
        validatePersonFields(request.name, request.surname, request.birthDate)
        return repository.update(id, request)
            ?: throw ResourceNotFoundException("Tracking session", id)
    }

    override suspend fun delete(id: String) {
        if (!repository.delete(id)) {
            throw ResourceNotFoundException("Tracking session", id)
        }
    }

    private fun validateSessionTimes(start: Long, end: Long) {
        if (end < start) {
            throw ValidationException("sessionEndTime must be greater than or equal to sessionStartTime")
        }
    }

    private fun validatePersonFields(name: String, surname: String, birthDate: String) {
        if (name.isBlank() || surname.isBlank() || birthDate.isBlank()) {
            throw ValidationException("name, surname, and birthDate must not be blank")
        }
        if (!BIRTH_DATE_REGEX.matches(birthDate)) {
            throw ValidationException("birthDate must be in DD/MM/YYYY format")
        }
    }

    companion object {
        private val BIRTH_DATE_REGEX = Regex(
            """^(0[1-9]|[12][0-9]|3[01])\/(0[1-9]|1[0-2])\/(19\d{2}|20([01]\d|2[0-5]))$"""
        )
    }
}
