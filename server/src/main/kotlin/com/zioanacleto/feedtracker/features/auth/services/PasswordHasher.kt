package com.zioanacleto.feedtracker.features.auth.services

import at.favre.lib.crypto.bcrypt.BCrypt

interface PasswordHasher {
    fun hash(raw: String): String
    fun verify(raw: String, hash: String): Boolean
}

class BcryptPasswordHasher : PasswordHasher {
    override fun hash(raw: String): String = BCrypt.withDefaults().hashToString(COST, raw.toCharArray())

    override fun verify(raw: String, hash: String): Boolean = BCrypt.verifyer().verify(raw.toCharArray(), hash).verified

    companion object {
        private const val COST = 12
    }
}
