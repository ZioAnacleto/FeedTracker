package com.zioanacleto.feedtracker.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import com.zioanacleto.feedtracker.domain.preferences.appLanguageFromTag
import java.util.Locale

private val FallbackAppLocale = staticCompositionLocalOf { Locale.getDefault().toLanguageTag() }
private var systemLocale: Locale? = null

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = FallbackAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val locale = applyJvmAppLanguage(appLanguageFromTag(value))
        return FallbackAppLocale.provides(locale.toLanguageTag())
    }
}

fun applyJvmAppLanguage(language: AppLanguage): Locale {
    if (systemLocale == null) {
        systemLocale = Locale.getDefault()
    }
    val locale = when (language) {
        AppLanguage.SYSTEM -> systemLocale ?: Locale.getDefault()
        AppLanguage.ENGLISH -> Locale.ENGLISH
        AppLanguage.ITALIAN -> Locale.forLanguageTag("it")
    }
    Locale.setDefault(locale)
    return locale
}
