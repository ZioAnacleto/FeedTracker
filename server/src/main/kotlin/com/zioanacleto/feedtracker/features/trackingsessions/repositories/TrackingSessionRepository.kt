package com.zioanacleto.feedtracker.features.trackingsessions.repositories

import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest

interface TrackingSessionRepository {
    suspend fun findAll(userId: String): List<TrackingSessionModel>
    suspend fun findById(userId: String, id: String): TrackingSessionModel?
    suspend fun create(userId: String, request: CreateTrackingSessionRequest): TrackingSessionModel
    suspend fun update(userId: String, id: String, request: UpdateTrackingSessionRequest): TrackingSessionModel?
    suspend fun delete(userId: String, id: String): Boolean
}
