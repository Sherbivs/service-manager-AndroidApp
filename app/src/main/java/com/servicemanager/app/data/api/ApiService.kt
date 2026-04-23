package com.servicemanager.app.data.api

import com.servicemanager.app.data.model.ActionResponseDto
import com.servicemanager.app.data.model.LogsResponseDto
import com.servicemanager.app.data.model.ServiceDto
import com.servicemanager.app.data.model.SystemInfoDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/services")
    suspend fun getServices(): List<ServiceDto>

    @POST("api/services/{id}/start")
    suspend fun startService(
        @Path("id") id: String,
    ): ActionResponseDto

    @POST("api/services/{id}/stop")
    suspend fun stopService(
        @Path("id") id: String,
    ): ActionResponseDto

    @POST("api/services/{id}/restart")
    suspend fun restartService(
        @Path("id") id: String,
    ): ActionResponseDto

    @GET("api/services/{id}/logs")
    suspend fun getServiceLogs(
        @Path("id") id: String,
        @Query("lines") lines: Int = 100,
    ): LogsResponseDto

    @GET("api/system")
    suspend fun getSystemInfo(): SystemInfoDto

    @GET("api/logs")
    suspend fun getGlobalLogs(
        @Query("lines") lines: Int = 100,
    ): LogsResponseDto

    @GET("api/services/{id}/logs/archive")
    suspend fun searchArchiveLogs(
        @Path("id") id: String,
        @Query("q") query: String,
    ): LogsResponseDto
}
