package com.servicemanager.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicemanager.app.data.repository.ServiceRepository
import com.servicemanager.app.util.EncryptedPrefsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val prefs: EncryptedPrefsHelper,
        private val repository: ServiceRepository,
    ) : ViewModel() {
        val currentUrl: String get() = prefs.serverUrl

        private val _saved = MutableSharedFlow<Unit>(replay = 0)
        val saved: SharedFlow<Unit> = _saved.asSharedFlow()

        private val _connectionTestStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
        val connectionTestStatus: StateFlow<ConnectionStatus> = _connectionTestStatus.asStateFlow()

        fun saveServerUrl(url: String) {
            prefs.serverUrl = url.trim().trimEnd('/')
            viewModelScope.launch { _saved.emit(Unit) }
        }

        fun testConnection(url: String) {
            viewModelScope.launch {
                _connectionTestStatus.value = ConnectionStatus.Loading
                val originalUrl = prefs.serverUrl
                prefs.serverUrl = url.trim().trimEnd('/')

                repository
                    .getSystemInfo()
                    .onSuccess {
                        _connectionTestStatus.value = ConnectionStatus.Success
                    }.onFailure {
                        _connectionTestStatus.value = ConnectionStatus.Error(it.message ?: "Unknown error")
                        // Optional: revert if we don't want to save yet
                        prefs.serverUrl = originalUrl
                    }
            }
        }

        sealed class ConnectionStatus {
            object Idle : ConnectionStatus()

            object Loading : ConnectionStatus()

            object Success : ConnectionStatus()

            data class Error(
                val message: String,
            ) : ConnectionStatus()
        }
    }
