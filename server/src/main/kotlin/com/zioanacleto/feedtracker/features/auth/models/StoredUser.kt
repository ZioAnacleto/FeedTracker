package com.zioanacleto.feedtracker.features.auth.models

import com.zioanacleto.feedtracker.domain.auth.UserModel

data class NewUser(
    val id: String,
    val email: String,
    val passwordHash: String?,
    val authMethod: String,
    val firstName: String,
    val lastName: String,
    val createdAt: Long,
)

data class StoredUser(val model: UserModel, val passwordHash: String?)
