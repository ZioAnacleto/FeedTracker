package com.zioanacleto.feedtracker.domain.core

sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String) : Resource<Nothing>
    object Loading : Resource<Nothing>
}