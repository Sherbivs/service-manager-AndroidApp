package com.servicemanager.app.data.model

import com.google.gson.annotations.SerializedName

data class SystemInfoDto(
    val hostname: String,
    val platform: String,
    @SerializedName("localIp") val localIp: String?,
    val uptime: Double,
    val nodeVersion: String?,
    val memory: MemoryInfoDto,
)

data class MemoryInfoDto(
    val used: Long,
    val total: Long,
    val free: Long,
)
