package com.zioanacleto.feedtracker.domain.auth

import kotlinx.serialization.Serializable

@Serializable
enum class AuthMethod {
    EMAIL,
    GOOGLE,
    APPLE,
}
