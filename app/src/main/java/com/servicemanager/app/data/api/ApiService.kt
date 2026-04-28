package com.servicemanager.app.data.api

import com.servicemanager.app.data.model.ActionResponseDto
import com.servicemanager.app.data.model.ArchiveResponseDto
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

    @POST("api/services/{id}/reset-circuit-breaker")
    suspend fun resetCircuitBreaker(
        @Path("id") id: String,
    ): ActionResponseDto

    @GET("api/system")
    suspend fun getSystemInfo(): SystemInfoDto

    @GET("api/logs")
    suspend fun getGlobalLogs(
        @Query("lines") lines: Int = 100,
    ): List<String>

    @GET("api/services/{id}/logs/archive")
    suspend fun searchArchiveLogs(
        @Path("id") id: String,
        @Query("q") query: String = "",
        @Query("level") level: String = "",
        @Query("from") from: Long = 0,
        @Query("to") to: Long = 0,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): ArchiveResponseDto

    @GET("api/logs/archive")
    suspend fun searchGlobalArchiveLogs(
        @Query("q") query: String = "",
        @Query("project") project: String = "",
        @Query("level") level: String = "",
        @Query("from") from: Long = 0,
        @Query("to") to: Long = 0,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
    ): ArchiveResponseDto

    @GET("api/logs/projects")
    suspend fun getLogProjects(): List<String>
}
