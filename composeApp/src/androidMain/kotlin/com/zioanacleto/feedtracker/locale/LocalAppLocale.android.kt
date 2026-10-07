package com.zioanacleto.feedtracker.locale

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.zioanacleto.feedtracker.domain.preferences.AppLanguage
import com.zioanacleto.feedtracker.domain.preferences.appLanguageFromTag
import com.zioanacleto.feedtracker.widget.TrackingSessionNotificationController
import com.zioanacleto.feedtracker.widget.TrackingSessionWidgetProvider
import java.util.Locale

private var systemLocale: Locale? = null
private var appliedLanguage: AppLanguage? = null

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = Locale.getDefault().toLanguageTag()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val locale = applyAndroidAppLanguage(LocalContext.current, appLanguageFromTag(value))
        val configuration = Configuration(LocalConfiguration.current)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return LocalConfiguration.provides(configuration)
    }
}

fun applyAndroidAppLanguage(context: Context, language: AppLanguage): Locale {
    if (systemLocale == null) {
        val locales = context.resources.configuration.locales
        systemLocale = if (locales.isEmpty) Locale.getDefault() else locales[0]
    }
    val locale = when (language) {
        AppLanguage.SYSTEM -> systemLocale ?: Locale.getDefault()
        AppLanguage.ENGLISH -> Locale.ENGLISH
        AppLanguage.ITALIAN -> Locale.forLanguageTag("it")
    }
    Locale.setDefault(locale)
    if (configurationDiffers(context, locale)) {
        updateConfiguration(context, locale)
    }
    val appContext = context.applicationContext
    if (appContext !== context && configurationDiffers(appContext, locale)) {
        updateConfiguration(appContext, locale)
    }
    if (appliedLanguage != language) {
        appliedLanguage = language
        TrackingSessionWidgetProvider.updateAll(appContext)
        TrackingSessionNotificationController.sync(appContext)
    }
    return locale
}

private fun configurationDiffers(context: Context, locale: Locale): Boolean {
    val current = context.resources.configuration.locales
    if (current.isEmpty) return true
    val active = current[0]
    return active.language != locale.language || active.country != locale.country
}

@Suppress("DEPRECATION")
private fun updateConfiguration(context: Context, locale: Locale) {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    configuration.setLocales(LocaleList(locale))
    configuration.setLayoutDirection(locale)
    context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
}
