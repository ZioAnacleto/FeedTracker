package com.zioanacleto.feedtracker.features.auth.models

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.UserModel
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUserModel(authMethods: List<AuthMethod>): UserModel = UserModel(
    id = this[UsersTable.id],
    email = this[UsersTable.email],
    authMethods = authMethods,
    firstName = this[UsersTable.firstName],
    lastName = this[UsersTable.lastName],
)

fun ResultRow.toEmailVerificationCode(): EmailVerificationCode = EmailVerificationCode(
    id = this[EmailVerificationCodesTable.id],
    email = this[EmailVerificationCodesTable.email],
    codeHash = this[EmailVerificationCodesTable.codeHash],
    expiresAt = this[EmailVerificationCodesTable.expiresAt],
    attemptCount = this[EmailVerificationCodesTable.attemptCount],
    consumedAt = this[EmailVerificationCodesTable.consumedAt],
    createdAt = this[EmailVerificationCodesTable.createdAt],
)
