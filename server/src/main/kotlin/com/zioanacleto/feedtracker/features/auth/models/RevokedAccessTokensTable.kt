package com.zioanacleto.feedtracker.features.auth.models

import org.jetbrains.exposed.sql.Table

object RevokedAccessTokensTable : Table("revoked_access_tokens") {
    val jti = varchar("jti", 36)
    val expiresAt = long("expires_at")
    val revokedAt = long("revoked_at")

    override val primaryKey = PrimaryKey(jti)
}
