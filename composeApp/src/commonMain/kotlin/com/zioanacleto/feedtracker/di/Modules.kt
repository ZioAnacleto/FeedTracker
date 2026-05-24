package com.zioanacleto.feedtracker.di

import com.zioanacleto.feedtracker.features.newtracking.NewTrackingViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module

fun initKoin(configuration: KoinAppDeclaration? = null): KoinApplication {
    return startKoin {
        includes(configuration)
        modules(sharedModule + uiModule + platformModule)
    }
}

val uiModule = module {
    viewModel { NewTrackingViewModel(get(), get()) }
}

expect val platformModule: Module