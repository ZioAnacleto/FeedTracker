package com.zioanacleto.feedtracker

import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import com.zioanacleto.feedtracker.network.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.NetworkInterface

class JvmNetworkMonitor(
    private val dispatcherProvider: DispatcherProvider
) : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flow {
        while (true) {
            emit(checkIsOnline())
            delay(5000) // Poll every 5 seconds
        }
    }
        .flowOn(dispatcherProvider.io())

    private fun checkIsOnline(): Boolean {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return false
            var isConnected = false
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    isConnected = true
                    break
                }
            }
            isConnected
        } catch (e: Exception) {
            false
        }
    }
}
