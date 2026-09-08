package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.features.home.HomeViewModel
import com.zioanacleto.feedtracker.features.login.EmailLoginViewModel
import com.zioanacleto.feedtracker.features.login.EmailSignUpViewModel
import com.zioanacleto.feedtracker.features.login.LoginMethodsViewModel
import com.zioanacleto.feedtracker.features.newtracking.NewTrackingViewModel
import com.zioanacleto.feedtracker.features.settings.PersonalSettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

fun initKoin(configuration: KoinAppDeclaration? = null): KoinApplication = startKoin {
    includes(configuration)
    modules(sharedModule + uiModule + platformModule)
}

val uiModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { NewTrackingViewModel(get()) }
    viewModel { LoginMethodsViewModel(get()) }
    viewModel { EmailLoginViewModel(get(), get()) }
    viewModel { EmailSignUpViewModel(get(), get()) }
    viewModel { PersonalSettingsViewModel(get(), get()) }
}

expect val platformModule: Module
