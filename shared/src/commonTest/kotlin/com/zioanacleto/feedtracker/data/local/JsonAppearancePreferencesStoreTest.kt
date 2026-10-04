package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.data.repositories.AppearancePreferencesRepositoryImpl
import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsonAppearancePreferencesStoreTest {

    @Test
    fun missingOrInvalidFileFallsBackToSystem() {
        val textStore = AppearanceFileTextStore()
        val store = JsonAppearancePreferencesStore(textStore)

        store.load() shouldBe AppearanceMode.SYSTEM

        textStore.write("not-json")
        store.load() shouldBe AppearanceMode.SYSTEM

        textStore.write("""{"mode":"SEPIA"}""")
        store.load() shouldBe AppearanceMode.SYSTEM
    }

    @Test
    fun saveAndLoadRoundTrip() {
        val textStore = AppearanceFileTextStore()
        val store = JsonAppearancePreferencesStore(textStore)

        store.save(AppearanceMode.LIGHT)
        store.load() shouldBe AppearanceMode.LIGHT

        val reloaded = AppearancePreferencesRepositoryImpl(JsonAppearancePreferencesStore(textStore))
        reloaded.mode.value shouldBe AppearanceMode.LIGHT
        reloaded.setMode(AppearanceMode.DARK)
        store.load() shouldBe AppearanceMode.DARK
    }
}

private class AppearanceFileTextStore : LocalTextStore {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }

    override fun delete() {
        value = null
    }
}
