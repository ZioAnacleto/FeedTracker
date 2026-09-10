package com.zioanacleto.feedtracker.features.auth.repositories

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.features.auth.models.NewUser
import com.zioanacleto.feedtracker.features.auth.models.StoredUser

interface UserRepository {
    suspend fun findByEmail(email: String): StoredUser?
    suspend fun findById(userId: String): UserModel?
    suspend fun findStoredById(userId: String): StoredUser?
    suspend fun create(user: NewUser): UserModel
    suspend fun addAuthMethod(userId: String, method: AuthMethod, passwordHash: String? = null): UserModel
    suspend fun updateNames(userId: String, firstName: String, lastName: String): UserModel
    suspend fun updatePassword(userId: String, passwordHash: String, tokensValidAfter: Long): UserModel
}
