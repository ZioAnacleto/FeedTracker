package com.zioanacleto.feedtracker.config

import com.zioanacleto.feedtracker.features.auth.repositories.EmailVerificationRepository
import com.zioanacleto.feedtracker.features.auth.repositories.EmailVerificationRepositoryImpl
import com.zioanacleto.feedtracker.features.auth.repositories.RevokedAccessTokenRepository
import com.zioanacleto.feedtracker.features.auth.repositories.RevokedAccessTokenRepositoryImpl
import com.zioanacleto.feedtracker.features.auth.repositories.UserRepository
import com.zioanacleto.feedtracker.features.auth.repositories.UserRepositoryImpl
import com.zioanacleto.feedtracker.features.auth.services.AuthService
import com.zioanacleto.feedtracker.features.auth.services.AuthServiceImpl
import com.zioanacleto.feedtracker.features.auth.services.BcryptPasswordHasher
import com.zioanacleto.feedtracker.features.auth.services.EmailSender
import com.zioanacleto.feedtracker.features.auth.services.JwksSocialTokenVerifier
import com.zioanacleto.feedtracker.features.auth.services.JwtTokenService
import com.zioanacleto.feedtracker.features.auth.services.LoggingEmailSender
import com.zioanacleto.feedtracker.features.auth.services.PasswordHasher
import com.zioanacleto.feedtracker.features.auth.services.SecureVerificationCodeGenerator
import com.zioanacleto.feedtracker.features.auth.services.SmtpEmailSender
import com.zioanacleto.feedtracker.features.auth.services.SocialTokenVerifier
import com.zioanacleto.feedtracker.features.auth.services.TimeProvider
import com.zioanacleto.feedtracker.features.auth.services.TokenService
import com.zioanacleto.feedtracker.features.auth.services.VerificationCodeGenerator
import com.zioanacleto.feedtracker.features.trackingpreferences.repositories.TrackingPreferencesRepository
import com.zioanacleto.feedtracker.features.trackingpreferences.repositories.TrackingPreferencesRepositoryImpl
import com.zioanacleto.feedtracker.features.trackingsessions.repositories.TrackingSessionRepository
import com.zioanacleto.feedtracker.features.trackingsessions.repositories.TrackingSessionRepositoryImpl
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionServiceImpl
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI(extraModules: List<Module> = emptyList()) {
    val appConfig = HoconAppConfig(environment.config)

    install(Koin) {
        allowOverride(true)
        slf4jLogger()
        modules(
            module {
                single<AppConfig> { appConfig }
                single { appConfig.database }
                single { appConfig.auth }
                single { appConfig.smtp }
                single<TimeProvider> { TimeProvider.System }
                single<PasswordHasher> { BcryptPasswordHasher() }
                single<TokenService> { JwtTokenService(appConfig.auth.jwtSecret, get()) }
                single<VerificationCodeGenerator> { SecureVerificationCodeGenerator() }
                single<EmailSender> {
                    if (appConfig.smtp.enabled) SmtpEmailSender(appConfig.smtp) else LoggingEmailSender()
                }
                single<SocialTokenVerifier> { JwksSocialTokenVerifier(appConfig.auth, get()) }
                single<UserRepository> { UserRepositoryImpl() }
                single<EmailVerificationRepository> { EmailVerificationRepositoryImpl() }
                single<RevokedAccessTokenRepository> { RevokedAccessTokenRepositoryImpl() }
                single<TrackingPreferencesRepository> { TrackingPreferencesRepositoryImpl() }
                single<AuthService> {
                    AuthServiceImpl(
                        users = get(),
                        verifications = get(),
                        emailSender = get(),
                        passwordHasher = get(),
                        tokens = get(),
                        codes = get(),
                        socialVerifier = get(),
                        timeProvider = get(),
                        authConfig = appConfig.auth,
                        revokedTokens = get(),
                        trackingPreferences = get(),
                    )
                }
                single<TrackingSessionRepository> { TrackingSessionRepositoryImpl() }
                single<TrackingSessionService> { TrackingSessionServiceImpl(get()) }
            } + extraModules,
        )
    }
}
