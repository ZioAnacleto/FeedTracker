package com.zioanacleto.feedtracker.features.auth.services

fun interface TimeProvider {
    fun nowMillis(): Long

    companion object {
        val System: TimeProvider = TimeProvider { java.lang.System.currentTimeMillis() }
    }
}

fun interface VerificationCodeGenerator {
    fun generate(): String
}

class SecureVerificationCodeGenerator : VerificationCodeGenerator {
    private val random = java.security.SecureRandom()

    override fun generate(): String = random.nextInt(1_000_000).toString().padStart(6, '0')
}
