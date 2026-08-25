package com.zioanacleto.feedtracker.di

import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named

actual inline fun <reified T> getNamedClass(): StringQualifier {
    return named(T::class.simpleName ?: T::class.toString())
}

actual inline fun <reified Interface : Any, reified Implementation : Interface> Module.factoryNamedClass(): KoinDefinition<Interface> {
    return factory<Interface>(
        named(
            Implementation::class.simpleName ?: Implementation::class.toString()
        )
    ) {
        get<Implementation>()
    }
}
