package com.servicemanager.app.data.model

data class ServiceDto(
    val id: String,
    val name: String,
    val project: String?,
    val description: String?,
    val type: String?,
    val url: String?,
    val healthCheck: String?,
    val autoRestart: Boolean,
    val status: String,
    val managed: Boolean,
    val pid: Int?,
    val startedAt: String?,
    val restartCount: Int = 0,
    val lastCheck: HealthCheckDto?,
    val healthPercent: Int?,
    val avgLatency: Int?,
    val consecutiveFailures: Int = 0,
    val circuitBreakerTripped: Boolean = false,
)

data class HealthCheckDto(
    val latency: Int,
    val statusCode: Int?,
    val healthy: Boolean,
    val reachable: Boolean,
    val timestamp: String,
)
