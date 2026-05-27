package com.zioanacleto.feedtracker.features.trackingsessions.repositories

import com.zioanacleto.feedtracker.common.utils.transactionDb
import com.zioanacleto.feedtracker.domain.CreateTrackingSessionRequest
import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import com.zioanacleto.feedtracker.domain.UpdateTrackingSessionRequest
import com.zioanacleto.feedtracker.features.trackingsessions.models.TrackingSessionsTable
import com.zioanacleto.feedtracker.features.trackingsessions.models.toModel
import com.zioanacleto.feedtracker.features.trackingsessions.models.toTrackingSessionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class TrackingSessionRepositoryImpl : TrackingSessionRepository {

    override suspend fun findAll(): List<TrackingSessionModel> = transactionDb {
        TrackingSessionsTable
            .selectAll()
            .map { it.toTrackingSessionModel() }
    }

    override suspend fun findById(id: String): TrackingSessionModel? =
        transactionDb {
            TrackingSessionsTable
                .selectAll()
                .where { TrackingSessionsTable.id eq id }
                .map { it.toTrackingSessionModel() }
                .singleOrNull()
        }

    override suspend fun create(request: CreateTrackingSessionRequest): TrackingSessionModel =
        with(request.toModel()) {
            transactionDb {
                TrackingSessionsTable.insert {
                    it[TrackingSessionsTable.id] = id
                    it[TrackingSessionsTable.sessionStartTime] = sessionStartTime
                    it[TrackingSessionsTable.sessionEndTime] = sessionEndTime
                    it[TrackingSessionsTable.name] = name
                    it[TrackingSessionsTable.surname] = surname
                    it[TrackingSessionsTable.birthDate] = birthDate
                    it[TrackingSessionsTable.additionalNotes] = additionalNotes
                }
            }
            request.toModel()
        }

    override suspend fun update(
        id: String,
        request: UpdateTrackingSessionRequest
    ): TrackingSessionModel? =
        withContext(Dispatchers.IO) {
            val model = request.toModel(id)
            val updatedRows = transaction {
                TrackingSessionsTable.update({ TrackingSessionsTable.id eq id }) {
                    it[TrackingSessionsTable.sessionStartTime] = model.sessionStartTime
                    it[TrackingSessionsTable.sessionEndTime] = model.sessionEndTime
                    it[TrackingSessionsTable.name] = model.name
                    it[TrackingSessionsTable.surname] = model.surname
                    it[TrackingSessionsTable.birthDate] = model.birthDate
                    it[TrackingSessionsTable.additionalNotes] = model.additionalNotes
                }
            }
            if (updatedRows == 0) null else model
        }

    override suspend fun delete(id: String): Boolean = transactionDb {
        TrackingSessionsTable.deleteWhere { TrackingSessionsTable.id eq id } > 0
    }
}
