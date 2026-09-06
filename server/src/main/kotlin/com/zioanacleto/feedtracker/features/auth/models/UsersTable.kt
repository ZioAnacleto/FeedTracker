package com.zioanacleto.feedtracker.features.auth.models

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val authMethod = varchar("auth_method", 32)
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}
