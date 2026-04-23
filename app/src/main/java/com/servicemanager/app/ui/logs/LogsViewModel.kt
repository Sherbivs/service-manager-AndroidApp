package com.servicemanager.app.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicemanager.app.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LogsUiState {
    object Loading : LogsUiState()

    data class Success(
        val lines: List<String>,
    ) : LogsUiState()

    data class Error(
        val message: String,
    ) : LogsUiState()
}

sealed class ArchiveUiState {
    object Idle : ArchiveUiState()

    object Loading : ArchiveUiState()

    data class Success(
        val lines: List<String>,
    ) : ArchiveUiState()

    data class Error(
        val message: String,
    ) : ArchiveUiState()
}

@HiltViewModel
class LogsViewModel
    @Inject
    constructor(
        private val repo: ServiceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<LogsUiState>(LogsUiState.Loading)
        val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

        private val _archiveState = MutableStateFlow<ArchiveUiState>(ArchiveUiState.Idle)
        val archiveState: StateFlow<ArchiveUiState> = _archiveState.asStateFlow()

        init {
            loadLogs()
        }

        fun loadLogs(lines: Int = 100) {
            viewModelScope.launch {
                _uiState.value = LogsUiState.Loading
                repo
                    .getGlobalLogs(lines)
                    .onSuccess { _uiState.value = LogsUiState.Success(it) }
                    .onFailure { _uiState.value = LogsUiState.Error(it.message ?: "Failed to load logs") }
            }
        }

        fun searchArchive(
            serviceId: String,
            query: String,
        ) {
            viewModelScope.launch {
                _archiveState.value = ArchiveUiState.Loading
                repo
                    .searchArchiveLogs(serviceId, query)
                    .onSuccess { _archiveState.value = ArchiveUiState.Success(it) }
                    .onFailure { _archiveState.value = ArchiveUiState.Error(it.message ?: "Archive search failed") }
            }
        }
    }
