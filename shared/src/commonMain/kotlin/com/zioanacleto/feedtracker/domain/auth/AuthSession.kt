package com.zioanacleto.feedtracker.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(val user: UserModel, val accessToken: String)
