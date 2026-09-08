package com.zioanacleto.feedtracker.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(val id: String, val email: String, val authMethods: List<AuthMethod>, val firstName: String, val lastName: String)
