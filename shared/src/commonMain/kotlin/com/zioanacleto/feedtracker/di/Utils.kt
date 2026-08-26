package com.zioanacleto.feedtracker.di

import org.koin.core.definition.KoinDefinition
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier

expect inline fun <reified T> getNamedClass(): StringQualifier

expect inline fun <reified Interface : Any, reified Implementation : Interface> Module.factoryNamedClass(): KoinDefinition<Interface>
