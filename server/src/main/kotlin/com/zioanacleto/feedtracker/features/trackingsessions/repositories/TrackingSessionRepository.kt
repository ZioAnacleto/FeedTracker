package com.zioanacleto.feedtracker.features.trackingsessions.repositories

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest

interface TrackingSessionRepository {
    suspend fun findAll(): List<TrackingSessionModel>
    suspend fun findById(id: String): TrackingSessionModel?
    suspend fun create(request: CreateTrackingSessionRequest): TrackingSessionModel
    suspend fun update(id: String, request: UpdateTrackingSessionRequest): TrackingSessionModel?
    suspend fun delete(id: String): Boolean
}
