package com.zioanacleto.feedtracker.features.auth.models

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import org.jetbrains.exposed.sql.Table

object UserAuthMethodsTable : Table("user_auth_methods") {
    val userId = varchar("user_id", 36).index()
    val method = varchar("method", 32)

    override val primaryKey = PrimaryKey(userId, method)
}

fun parseAuthMethods(values: Collection<String>, fallback: String): List<AuthMethod> {
    val methods = values.map { AuthMethod.valueOf(it) }.ifEmpty { listOf(AuthMethod.valueOf(fallback)) }
    return methods.distinct().sortedBy { it.ordinal }
}
