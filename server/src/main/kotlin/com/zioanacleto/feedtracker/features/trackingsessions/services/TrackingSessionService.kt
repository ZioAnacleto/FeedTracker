package com.zioanacleto.feedtracker.features.trackingsessions.services

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest

interface TrackingSessionService {
    suspend fun getAll(userId: String): List<TrackingSessionModel>
    suspend fun getById(userId: String, id: String): TrackingSessionModel
    suspend fun create(userId: String, request: CreateTrackingSessionRequest): TrackingSessionModel
    suspend fun update(userId: String, id: String, request: UpdateTrackingSessionRequest): TrackingSessionModel
    suspend fun delete(userId: String, id: String)
}
