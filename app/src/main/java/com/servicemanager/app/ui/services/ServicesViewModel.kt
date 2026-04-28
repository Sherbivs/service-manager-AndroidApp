package com.servicemanager.app.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicemanager.app.data.model.ServiceDto
import com.servicemanager.app.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ServicesUiState {
    object Loading : ServicesUiState()

    data class Success(
        val services: List<ServiceDto>,
    ) : ServicesUiState()

    data class Error(
        val message: String,
    ) : ServicesUiState()
}

@HiltViewModel
class ServicesViewModel
    @Inject
    constructor(
        private val repo: ServiceRepository,
    ) : ViewModel() {
        companion object {
            private const val POLLING_INTERVAL_MS = 10000L
            private const val STALE_THRESHOLD_MS = 10000L
        }

        private val _uiState = MutableStateFlow<ServicesUiState>(ServicesUiState.Loading)
        val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

        private val _actionError = MutableSharedFlow<String>(replay = 0)
        val actionError: SharedFlow<String> = _actionError.asSharedFlow()

        private val _actionSuccess = MutableSharedFlow<String>(replay = 0)
        val actionSuccess: SharedFlow<String> = _actionSuccess.asSharedFlow()

        private val _pendingActions = MutableStateFlow<Set<String>>(emptySet())
        val pendingActions: StateFlow<Set<String>> = _pendingActions.asStateFlow()

        private var refreshJob: Job? = null
        private var lastFetchTimeMs = 0L
        private var lastFailedAction: (() -> Unit)? = null

        init {
            loadServicesIfStale()
        }

        fun retryLastAction() {
            lastFailedAction?.invoke()
        }

        /** Pull-to-refresh and explicit reload — always fetches regardless of freshness. */
        fun loadServices() {
            viewModelScope.launch {
                _uiState.value = ServicesUiState.Loading
                fetchServices()
            }
        }

        /**
         * Called on init and after rotation. Skips the network call if the current state is
         * [ServicesUiState.Success] and data was fetched within [STALE_THRESHOLD_MS].
         */
        fun loadServicesIfStale() {
            val dataIsFresh =
                (
                    _uiState.value is ServicesUiState.Success &&
                        System.currentTimeMillis() - lastFetchTimeMs < STALE_THRESHOLD_MS
                )
            if (dataIsFresh) return
            viewModelScope.launch {
                if (_uiState.value !is ServicesUiState.Success) {
                    _uiState.value = ServicesUiState.Loading
                }
                fetchServices()
            }
        }

        private suspend fun fetchServices() {
            repo
                .getServices()
                .onSuccess {
                    lastFetchTimeMs = System.currentTimeMillis()
                    _uiState.value = ServicesUiState.Success(it)
                }.onFailure {
                    if (_uiState.value is ServicesUiState.Loading || _uiState.value is ServicesUiState.Error) {
                        _uiState.value = ServicesUiState.Error(it.message ?: "Failed to load services")
                    } else {
                        _actionError.emit(it.message ?: "Update failed")
                    }
                }
        }

        fun startPolling() {
            refreshJob?.cancel()
            refreshJob =
                viewModelScope.launch {
                    while (true) {
                        fetchServices()
                        delay(POLLING_INTERVAL_MS)
                    }
                }
        }

        fun stopPolling() {
            refreshJob?.cancel()
        }

        fun startService(id: String) =
            triggerAction(id, repo::startService, "Service started", "Failed to start service")

        fun stopService(id: String) = triggerAction(id, repo::stopService, "Service stopped", "Failed to stop service")

        fun restartService(id: String) =
            triggerAction(id, repo::restartService, "Service restarted", "Failed to restart service")

        fun resetCircuitBreaker(id: String) =
            triggerAction(id, repo::resetCircuitBreaker, "Circuit breaker reset", "Failed to reset circuit breaker")

        private fun triggerAction(
            id: String,
            action: suspend (String) -> Result<Unit>,
            successMessage: String,
            errorFallback: String,
        ) {
            val retryFn: () -> Unit = { triggerAction(id, action, successMessage, errorFallback) }
            viewModelScope.launch {
                _pendingActions.value += id
                action(id)
                    .onSuccess { _actionSuccess.emit(successMessage) }
                    .onFailure {
                        lastFailedAction = retryFn
                        _actionError.emit(it.message ?: errorFallback)
                    }
                _pendingActions.value -= id
                fetchServices()
            }
        }
    }
