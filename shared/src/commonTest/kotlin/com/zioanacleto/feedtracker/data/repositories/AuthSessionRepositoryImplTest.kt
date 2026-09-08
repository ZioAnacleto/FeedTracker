package com.zioanacleto.feedtracker.data.repositories

import com.zioanacleto.feedtracker.data.local.InMemoryAuthSessionStore
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AuthSessionRepositoryImplTest {

    @Test
    fun startsLoggedOutWhenStoreIsEmpty() {
        val repository = AuthSessionRepositoryImpl(InMemoryAuthSessionStore())

        repository.session.value.shouldBeNull()
    }

    @Test
    fun setSessionPersistsAndExposesSession() {
        val store = InMemoryAuthSessionStore()
        val repository = AuthSessionRepositoryImpl(store)
        val session = AuthSession(
            user = UserModel(
                id = "user-1",
                email = "mario@example.com",
                authMethods = listOf(AuthMethod.EMAIL),
                firstName = "Mario",
                lastName = "Rossi",
            ),
            accessToken = "access-token",
        )

        repository.setSession(session)

        repository.session.value shouldBe session
        store.load() shouldBe session
    }
}
