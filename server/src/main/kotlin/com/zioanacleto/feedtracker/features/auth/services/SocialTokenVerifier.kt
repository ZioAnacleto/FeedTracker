package com.zioanacleto.feedtracker.features.auth.services

data class SocialProfile(val email: String, val firstName: String, val lastName: String)

interface SocialTokenVerifier {
    suspend fun verifyGoogle(idToken: String): SocialProfile
    suspend fun verifyApple(identityToken: String): SocialProfile
}
