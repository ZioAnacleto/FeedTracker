package com.zioanacleto.feedtracker.features.trackingsessions.services

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest

interface TrackingSessionService {
    suspend fun getAll(): List<TrackingSessionModel>
    suspend fun getById(id: String): TrackingSessionModel
    suspend fun create(request: CreateTrackingSessionRequest): TrackingSessionModel
    suspend fun update(id: String, request: UpdateTrackingSessionRequest): TrackingSessionModel
    suspend fun delete(id: String)
}
