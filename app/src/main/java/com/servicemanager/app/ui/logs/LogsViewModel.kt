package com.servicemanager.app.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicemanager.app.data.model.ArchiveRowDto
import com.servicemanager.app.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
        val rows: List<ArchiveRowDto>,
        val total: Int,
        val limit: Int,
        val offset: Int,
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
        companion object {
            private const val ARCHIVE_PAGE_SIZE = 100
        }

        private data class ArchiveQuery(
            val serviceId: String? = null,
            val query: String = "",
            val project: String = "",
            val level: String = "",
            val limit: Int = ARCHIVE_PAGE_SIZE,
        )

        private val _uiState = MutableStateFlow<LogsUiState>(LogsUiState.Loading)
        val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

        private val _archiveState = MutableStateFlow<ArchiveUiState>(ArchiveUiState.Idle)
        val archiveState: StateFlow<ArchiveUiState> = _archiveState.asStateFlow()

        private val _availableServices = MutableStateFlow<List<String>>(emptyList())
        val availableServices: StateFlow<List<String>> = _availableServices.asStateFlow()

        private val _availableProjects = MutableStateFlow<List<String>>(emptyList())
        val availableProjects: StateFlow<List<String>> = _availableProjects.asStateFlow()

        private var archiveSearchJob: Job? = null
        private var activeArchiveQuery: ArchiveQuery? = null

        init {
            loadLogs()
            loadAvailableServices()
        }

        private fun loadAvailableServices() {
            viewModelScope.launch {
                repo.getServices().onSuccess { services ->
                    _availableServices.value = services.map { it.id }
                }
                repo.getLogProjects().onSuccess { projects ->
                    _availableProjects.value = projects
                }
            }
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
            level: String = "",
        ) {
            activeArchiveQuery = ArchiveQuery(serviceId = serviceId, query = query, level = level)
            executeArchiveSearch(offset = 0)
        }

        fun searchGlobalArchive(
            query: String = "",
            project: String = "",
            level: String = "",
        ) {
            activeArchiveQuery = ArchiveQuery(query = query, project = project, level = level)
            executeArchiveSearch(offset = 0)
        }

        fun loadNextArchivePage() {
            val current = _archiveState.value as? ArchiveUiState.Success ?: return
            val nextOffset = current.offset + current.limit
            if (nextOffset >= current.total) return
            executeArchiveSearch(offset = nextOffset)
        }

        fun loadPreviousArchivePage() {
            val current = _archiveState.value as? ArchiveUiState.Success ?: return
            val prevOffset = (current.offset - current.limit).coerceAtLeast(0)
            if (prevOffset == current.offset) return
            executeArchiveSearch(offset = prevOffset)
        }

        private fun executeArchiveSearch(offset: Int) {
            val query = activeArchiveQuery ?: return
            archiveSearchJob?.cancel()
            archiveSearchJob =
                viewModelScope.launch {
                _archiveState.value = ArchiveUiState.Loading
                val result =
                    if (!query.serviceId.isNullOrBlank()) {
                        repo.searchArchiveLogs(
                            serviceId = query.serviceId,
                            query = query.query,
                            level = query.level,
                            limit = query.limit,
                            offset = offset,
                        )
                    } else {
                        repo.searchGlobalArchiveLogs(
                            query = query.query,
                            project = query.project,
                            level = query.level,
                            limit = query.limit,
                            offset = offset,
                        )
                    }

                result
                    .onSuccess {
                        _archiveState.value =
                            ArchiveUiState.Success(
                                rows = it.rows,
                                total = it.total,
                                limit = it.limit,
                                offset = it.offset,
                            )
                    }.onFailure {
                        _archiveState.value = ArchiveUiState.Error(it.message ?: "Archive search failed")
                    }
                }
        }
    }
