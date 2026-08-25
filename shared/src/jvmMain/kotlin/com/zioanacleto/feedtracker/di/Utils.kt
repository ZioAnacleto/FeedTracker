package com.zioanacleto.feedtracker.di

import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

actual inline fun <reified T> getNamedClass(): StringQualifier = named(T::class.java.simpleName)

actual inline fun <reified Interface : Any, reified Implementation : Interface> Module.factoryNamedClass(): KoinDefinition<Interface> =
    factory<Interface>(
        named(
            Implementation::class.java.simpleName,
        ),
    ) {
        Implementation::class.java.newInstance()
    }
