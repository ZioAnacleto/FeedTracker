package com.zioanacleto.feedtracker.testutil

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.repositories.AuthRepository
import kotlinx.coroutines.delay

class FakeAuthRepository(
    private val methods: List<AuthMethod> = listOf(AuthMethod.EMAIL),
    private val methodsError: Throwable? = null,
    private val session: AuthSession = AuthSession(
        user = UserModel(
            id = "user-1",
            email = "mario@example.com",
            authMethods = listOf(AuthMethod.EMAIL),
            firstName = "Mario",
            lastName = "Rossi",
        ),
        accessToken = "access-token",
    ),
    private val loginError: Throwable? = null,
    private val startRegistrationError: Throwable? = null,
    private val verifyError: Throwable? = null,
    private val completeError: Throwable? = null,
    private val loginDelayMillis: Long = 0,
    private val startRegistrationDelayMillis: Long = 0,
    private val verifyDelayMillis: Long = 0,
    private val logoutDelayMillis: Long = 0,
    private val registrationToken: String = "reg-token",
    private val logoutError: Throwable? = null,
) : AuthRepository {
    var loginCalls = 0
        private set
    var startRegistrationCalls = 0
        private set
    var verifyCalls = 0
        private set
    var completeCalls = 0
        private set
    var lastStartedEmail: String? = null
        private set
    var lastVerifiedCode: String? = null
        private set
    var logoutCalls = 0
        private set
    var lastLogoutToken: String? = null
        private set

    override suspend fun getAvailableAuthMethods(): List<AuthMethod> {
        methodsError?.let { throw it }
        return methods
    }

    override suspend fun startEmailRegistration(email: String) {
        startRegistrationCalls += 1
        lastStartedEmail = email
        if (startRegistrationDelayMillis > 0) {
            delay(startRegistrationDelayMillis)
        }
        startRegistrationError?.let { throw it }
    }

    override suspend fun verifyEmailCode(email: String, code: String): String {
        verifyCalls += 1
        lastVerifiedCode = code
        if (verifyDelayMillis > 0) {
            delay(verifyDelayMillis)
        }
        verifyError?.let { throw it }
        return registrationToken
    }

    override suspend fun completeEmailRegistration(
        registrationToken: String,
        password: String,
        firstName: String,
        lastName: String,
    ): AuthSession {
        completeCalls += 1
        completeError?.let { throw it }
        return session
    }

    override suspend fun loginWithEmail(email: String, password: String): AuthSession {
        loginCalls += 1
        if (loginDelayMillis > 0) {
            delay(loginDelayMillis)
        }
        loginError?.let { throw it }
        return session
    }

    override suspend fun logout(accessToken: String) {
        logoutCalls += 1
        lastLogoutToken = accessToken
        if (logoutDelayMillis > 0) {
            delay(logoutDelayMillis)
        }
        logoutError?.let { throw it }
    }
}
