package com.servicemanager.app.ui.system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicemanager.app.data.model.SystemInfoDto
import com.servicemanager.app.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SystemUiState {
    object Loading : SystemUiState()

    data class Success(
        val info: SystemInfoDto,
    ) : SystemUiState()

    data class Error(
        val message: String,
    ) : SystemUiState()
}

@HiltViewModel
class SystemViewModel
    @Inject
    constructor(
        private val repo: ServiceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<SystemUiState>(SystemUiState.Loading)
        val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

        init {
            loadSystemInfo()
        }

        fun loadSystemInfo() {
            viewModelScope.launch {
                _uiState.value = SystemUiState.Loading
                repo
                    .getSystemInfo()
                    .onSuccess { _uiState.value = SystemUiState.Success(it) }
                    .onFailure { _uiState.value = SystemUiState.Error(it.message ?: "Failed to load system info") }
            }
        }
    }
