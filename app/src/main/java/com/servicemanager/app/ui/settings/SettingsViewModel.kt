package com.servicemanager.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicemanager.app.data.repository.ServiceRepository
import com.servicemanager.app.util.SecurePrefsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val prefs: SecurePrefsHelper,
        private val repository: ServiceRepository,
    ) : ViewModel() {
        val currentUrl: String get() = prefs.serverUrl
        val currentServerScheme: String get() = prefs.serverScheme
        val currentServerHost: String get() = prefs.serverHost
        val currentServerPort: Int get() = prefs.serverPort
        val currentConnectTimeoutSeconds: Int get() = prefs.connectTimeoutSeconds
        val currentReadTimeoutSeconds: Int get() = prefs.readTimeoutSeconds

        private val _saved = MutableSharedFlow<Unit>(replay = 0)
        val saved: SharedFlow<Unit> = _saved.asSharedFlow()

        private val _connectionTestStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
        val connectionTestStatus: StateFlow<ConnectionStatus> = _connectionTestStatus.asStateFlow()

        private val _pingResult = MutableStateFlow<PingStatus>(PingStatus.Idle)
        val pingResult: StateFlow<PingStatus> = _pingResult.asStateFlow()

        fun saveNetworkSettings(
            serverScheme: String,
            serverHost: String,
            serverPort: Int,
            connectTimeoutSeconds: Int,
            readTimeoutSeconds: Int,
        ) {
            prefs.serverScheme = serverScheme
            prefs.serverHost = serverHost
            prefs.serverPort = serverPort
            prefs.connectTimeoutSeconds = connectTimeoutSeconds
            prefs.readTimeoutSeconds = readTimeoutSeconds
            viewModelScope.launch { _saved.emit(Unit) }
        }

        fun saveNetworkSettings(
            url: String,
            connectTimeoutSeconds: Int,
            readTimeoutSeconds: Int,
        ) {
            prefs.serverUrl = url.trim().trimEnd('/')
            prefs.connectTimeoutSeconds = connectTimeoutSeconds
            prefs.readTimeoutSeconds = readTimeoutSeconds
            viewModelScope.launch { _saved.emit(Unit) }
        }

        fun testConnection(
            serverScheme: String,
            serverHost: String,
            serverPort: Int,
            connectTimeoutSeconds: Int,
            readTimeoutSeconds: Int,
        ) {
            testConnection(
                url = composeUrl(serverScheme, serverHost, serverPort),
                connectTimeoutSeconds = connectTimeoutSeconds,
                readTimeoutSeconds = readTimeoutSeconds,
            )
        }

        fun testConnection(
            url: String,
            connectTimeoutSeconds: Int,
            readTimeoutSeconds: Int,
        ) {
            viewModelScope.launch {
                _connectionTestStatus.value = ConnectionStatus.Loading
                val originalUrl = prefs.serverUrl
                val originalConnectTimeoutSeconds = prefs.connectTimeoutSeconds
                val originalReadTimeoutSeconds = prefs.readTimeoutSeconds
                prefs.serverUrl = url.trim().trimEnd('/')
                prefs.connectTimeoutSeconds = connectTimeoutSeconds
                prefs.readTimeoutSeconds = readTimeoutSeconds

                repository
                    .getSystemInfo()
                    .onSuccess {
                        _connectionTestStatus.value = ConnectionStatus.Success
                    }.onFailure {
                        _connectionTestStatus.value = ConnectionStatus.Error(it.message ?: "Unknown error")
                        prefs.serverUrl = originalUrl
                        prefs.connectTimeoutSeconds = originalConnectTimeoutSeconds
                        prefs.readTimeoutSeconds = originalReadTimeoutSeconds
                    }
            }
        }

        fun pingServer(
            serverScheme: String,
            serverHost: String,
            serverPort: Int,
            connectTimeoutSeconds: Int,
            readTimeoutSeconds: Int,
        ) {
            pingServer(
                url = composeUrl(serverScheme, serverHost, serverPort),
                connectTimeoutSeconds = connectTimeoutSeconds,
                readTimeoutSeconds = readTimeoutSeconds,
            )
        }

        fun pingServer(
            url: String,
            connectTimeoutSeconds: Int,
            readTimeoutSeconds: Int,
        ) {
            viewModelScope.launch {
                _pingResult.value = PingStatus.Loading
                val originalUrl = prefs.serverUrl
                val originalConnectTimeoutSeconds = prefs.connectTimeoutSeconds
                val originalReadTimeoutSeconds = prefs.readTimeoutSeconds
                prefs.serverUrl = url.trim().trimEnd('/')
                prefs.connectTimeoutSeconds = connectTimeoutSeconds
                prefs.readTimeoutSeconds = readTimeoutSeconds

                val latencies = mutableListOf<Long>()
                var successCount = 0
                val count = 4

                for (i in 1..count) {
                    val time =
                        measureTimeMillis {
                            repository
                                .getSystemInfo()
                                .onSuccess { successCount++ }
                        }
                    if (successCount >= i) {
                        latencies.add(time)
                    }
                    if (i < count) delay(500)
                }

                if (latencies.isNotEmpty()) {
                    _pingResult.value =
                        PingStatus.Result(
                            min = latencies.minOrNull() ?: 0,
                            max = latencies.maxOrNull() ?: 0,
                            avg = latencies.average().toLong(),
                            successRate = (successCount.toFloat() / count * 100).toInt(),
                        )
                } else {
                    _pingResult.value = PingStatus.Error("All pings failed")
                }

                // Don't restore original URL if it was actually reachable
                if (successCount == 0) {
                    prefs.serverUrl = originalUrl
                    prefs.connectTimeoutSeconds = originalConnectTimeoutSeconds
                    prefs.readTimeoutSeconds = originalReadTimeoutSeconds
                }
            }
        }

        private fun composeUrl(
            scheme: String,
            host: String,
            port: Int,
        ): String = "${scheme.trim().lowercase()}://${host.trim()}:$port"

        sealed class ConnectionStatus {
            object Idle : ConnectionStatus()

            object Loading : ConnectionStatus()

            object Success : ConnectionStatus()

            data class Error(
                val message: String,
            ) : ConnectionStatus()
        }

        sealed class PingStatus {
            object Idle : PingStatus()

            object Loading : PingStatus()

            data class Result(
                val min: Long,
                val max: Long,
                val avg: Long,
                val successRate: Int,
            ) : PingStatus()

            data class Error(
                val message: String,
            ) : PingStatus()
        }
    }
