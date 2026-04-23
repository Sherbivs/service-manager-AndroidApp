package com.servicemanager.app.data.repository

import com.servicemanager.app.R
import com.servicemanager.app.data.api.ApiService
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

        suspend fun getServiceLogs(
            id: String,
            lines: Int = 100,
        ): Result<List<String>> = safeApiCall { api.getServiceLogs(id, lines).lines }

        suspend fun getSystemInfo(): Result<SystemInfoDto> = safeApiCall { api.getSystemInfo() }

        suspend fun getGlobalLogs(lines: Int = 100): Result<List<String>> =
            safeApiCall { api.getGlobalLogs(lines).lines }

        suspend fun searchArchiveLogs(
            serviceId: String,
            query: String,
        ): Result<List<String>> = safeApiCall { api.searchArchiveLogs(serviceId, query).lines }

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
