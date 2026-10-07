package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.data.repositories.LanguagePreferencesRepositoryImpl
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class JsonLanguagePreferencesStoreTest {

    @Test
    fun missingOrInvalidFileFallsBackToSystem() {
        val textStore = LanguageFileTextStore()
        val store = JsonLanguagePreferencesStore(textStore)

        store.load() shouldBe AppLanguage.SYSTEM

        textStore.write("not-json")
        store.load() shouldBe AppLanguage.SYSTEM

        textStore.write("""{"language":"FRENCH"}""")
        store.load() shouldBe AppLanguage.SYSTEM
    }

    @Test
    fun saveAndLoadRoundTripLeavesDateFormatAlone() {
        val textStore = LanguageFileTextStore()
        val store = JsonLanguagePreferencesStore(textStore)

        store.save(AppLanguage.ITALIAN)
        store.load() shouldBe AppLanguage.ITALIAN
        textStore.read().orEmpty().shouldNotContain("dateFormat")

        val reloaded = LanguagePreferencesRepositoryImpl(JsonLanguagePreferencesStore(textStore))
        reloaded.language.value shouldBe AppLanguage.ITALIAN
        reloaded.setLanguage(AppLanguage.ENGLISH)
        store.load() shouldBe AppLanguage.ENGLISH
    }
}

private class LanguageFileTextStore : LocalTextStore {
    private var value: String? = null

    override fun read(): String? = value

    override fun write(value: String) {
        this.value = value
    }

    override fun delete() {
        value = null
    }
}
