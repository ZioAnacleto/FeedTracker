package com.zioanacleto.feedtracker.features.auth.models

import org.jetbrains.exposed.sql.Table

object EmailVerificationCodesTable : Table("email_verification_codes") {
    val id = varchar("id", 36)
    val email = varchar("email", 255)
    val codeHash = varchar("code_hash", 255)
    val expiresAt = long("expires_at")
    val attemptCount = integer("attempt_count").default(0)
    val consumedAt = long("consumed_at").nullable()
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

data class EmailVerificationCode(
    val id: String,
    val email: String,
    val codeHash: String,
    val expiresAt: Long,
    val attemptCount: Int,
    val consumedAt: Long?,
    val createdAt: Long,
)
