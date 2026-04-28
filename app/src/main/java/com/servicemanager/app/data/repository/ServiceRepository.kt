package com.servicemanager.app.data.repository

import com.servicemanager.app.R
import com.servicemanager.app.data.api.ApiService
import com.servicemanager.app.data.model.ArchiveResponseDto
import com.servicemanager.app.data.model.ServiceDto
import com.servicemanager.app.data.model.SystemInfoDto
import com.servicemanager.app.util.ResourceProvider
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository
    @Inject
    constructor(
        private val api: ApiService,
        private val resourceProvider: ResourceProvider,
    ) {
        suspend fun getServices(): Result<List<ServiceDto>> = safeApiCall { api.getServices() }

        suspend fun startService(id: String): Result<Unit> =
            safeApiCall {
                val resp = api.startService(id)
                if (!resp.success) error(resp.message ?: "Start failed")
            }

        suspend fun stopService(id: String): Result<Unit> =
            safeApiCall {
                val resp = api.stopService(id)
                if (!resp.success) error(resp.message ?: "Stop failed")
            }

        suspend fun restartService(id: String): Result<Unit> =
            safeApiCall {
                val resp = api.restartService(id)
                if (!resp.success) error(resp.message ?: "Restart failed")
            }

        suspend fun resetCircuitBreaker(id: String): Result<Unit> =
            safeApiCall {
                val resp = api.resetCircuitBreaker(id)
                if (!resp.success) error(resp.message ?: "Reset failed")
            }

        suspend fun getSystemInfo(): Result<SystemInfoDto> = safeApiCall { api.getSystemInfo() }

        suspend fun getGlobalLogs(lines: Int = 100): Result<List<String>> = safeApiCall { api.getGlobalLogs(lines) }

        suspend fun searchArchiveLogs(
            serviceId: String,
            query: String,
            level: String = "",
            from: Long = 0,
            to: Long = 0,
            limit: Int = 100,
            offset: Int = 0,
        ): Result<ArchiveResponseDto> =
            safeApiCall { api.searchArchiveLogs(serviceId, query, level, from, to, limit, offset) }

        suspend fun searchGlobalArchiveLogs(
            query: String = "",
            project: String = "",
            level: String = "",
            from: Long = 0,
            to: Long = 0,
            limit: Int = 100,
            offset: Int = 0,
        ): Result<ArchiveResponseDto> = safeApiCall {
            api.searchGlobalArchiveLogs(query, project, level, from, to, limit, offset)
        }

        suspend fun getLogProjects(): Result<List<String>> = safeApiCall { api.getLogProjects() }

        private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
            val result = runCatching { call() }
            val throwable = result.exceptionOrNull() ?: return result
            return Result.failure(Exception(getErrorMessage(throwable)))
        }

        private fun getErrorMessage(throwable: Throwable): String =
            when (throwable) {
                is SocketTimeoutException -> resourceProvider.getString(R.string.error_timeout)
                is IOException -> resourceProvider.getString(R.string.error_network)
                is HttpException -> resourceProvider.getString(R.string.error_server, throwable.code())
                else -> throwable.message ?: resourceProvider.getString(R.string.error_unknown)
            }
    }
