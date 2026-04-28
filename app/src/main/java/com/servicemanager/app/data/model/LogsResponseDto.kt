package com.servicemanager.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Row returned by GET /api/services/:id/logs/archive and GET /api/logs/archive
 */
data class ArchiveRowDto(
    val id: Long,
    @SerializedName("service_id") val serviceId: String? = null,
    val project: String? = null,
    @SerializedName("log_level") val logLevel: String = "info",
    val line: String,
    @SerializedName("archived_at") val archivedAt: Long,
)

/**
 * Envelope returned by GET /api/services/:id/logs/archive and GET /api/logs/archive
 * Server shape: { rows: [...], total: N, limit: N, offset: N }
 */
data class ArchiveResponseDto(
    val rows: List<ArchiveRowDto>,
    val total: Int,
    val limit: Int,
    val offset: Int,
)
