package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsonAuthSessionStoreTest {

    @Test
    fun saveLoadAndClearRoundTrip() {
        val textStore = InMemoryTextStore()
        val store = JsonAuthSessionStore(textStore)
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

        store.load().shouldBeNull()
        store.save(session)
        store.load() shouldBe session
        store.clear()
        store.load().shouldBeNull()
    }
}

private class InMemoryTextStore : LocalTextStore {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }

    override fun delete() {
        value = null
    }
}
