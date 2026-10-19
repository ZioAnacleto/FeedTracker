package com.zioanacleto.feedtracker.config

import com.zioanacleto.feedtracker.features.trackingsessions.models.TrackingSessionsTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

object SchemaMigrationsTable : Table("schema_migrations") {
    val id = varchar("id", 128)

    override val primaryKey = PrimaryKey(id)
}

object DatabaseMigrator {
    private val migrations = listOf(
        "delete_tracking_sessions_with_null_user_id" to {
            TrackingSessionsTable.deleteWhere { TrackingSessionsTable.userId.isNull() }
        },
    )

    fun applyPending() {
        migrations.forEach { (id, apply) ->
            val alreadyApplied = SchemaMigrationsTable
                .selectAll()
                .where { SchemaMigrationsTable.id eq id }
                .count() > 0
            if (alreadyApplied) return@forEach
            apply()
            SchemaMigrationsTable.insert {
                it[SchemaMigrationsTable.id] = id
            }
        }
    }
}
