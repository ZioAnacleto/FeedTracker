package com.zioanacleto.feedtracker.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import com.zioanacleto.feedtracker.domain.preferences.appLanguageFromTag
import com.zioanacleto.feedtracker.domain.preferences.languageTag
import com.zioanacleto.feedtracker.domain.repositories.LanguagePreferencesRepository
import com.zioanacleto.feedtracker.widget.ActiveTrackingSessionDefaults
import com.zioanacleto.feedtracker.widget.reloadIosTrackingWidgets
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.mp.KoinPlatform
import platform.Foundation.NSLocale
import platform.Foundation.NSMutableArray
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

private val FallbackAppLocale = staticCompositionLocalOf { "en" }

actual object LocalAppLocale {
    private var systemLanguageTag: String? = null
    private var appliedLanguage: AppLanguage? = null

    actual val current: String
        @Composable get() = FallbackAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val language = appLanguageFromTag(value)
        applyIosAppLanguage(language)
        return FallbackAppLocale.provides(language.languageTag() ?: systemLanguageTag ?: "en")
    }

    @OptIn(ExperimentalForeignApi::class)
    fun apply(language: AppLanguage) {
        if (systemLanguageTag == null) {
            systemLanguageTag = (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
        }
        if (appliedLanguage == language) return
        appliedLanguage = language
        val tag = language.languageTag()
        val defaults = NSUserDefaults.standardUserDefaults
        if (tag == null) {
            defaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
        } else {
            defaults.setObject(nsArrayOf(tag), forKey = APPLE_LANGUAGES_KEY)
        }
        val group = NSUserDefaults(suiteName = ActiveTrackingSessionDefaults.APP_GROUP_ID)
        if (tag == null) {
            group.removeObjectForKey(ActiveTrackingSessionDefaults.APP_LANGUAGE_KEY)
        } else {
            group.setObject(tag, forKey = ActiveTrackingSessionDefaults.APP_LANGUAGE_KEY)
        }
        defaults.synchronize()
        group.synchronize()
        reloadIosTrackingWidgets()
    }
}

fun applyIosAppLanguage(language: AppLanguage) {
    LocalAppLocale.apply(language)
}

fun applyStoredIosLanguage() {
    val language = KoinPlatform.getKoin().get<LanguagePreferencesRepository>().language.value
    LocalAppLocale.apply(language)
}

@OptIn(ExperimentalForeignApi::class)
private fun nsArrayOf(value: String): NSMutableArray {
    val array = NSMutableArray()
    array.addObject(value)
    return array
}
