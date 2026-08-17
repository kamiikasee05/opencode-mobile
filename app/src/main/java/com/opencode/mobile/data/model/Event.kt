package com.opencode.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SseEvent(
    val type: String,
    val properties: JsonElement? = null
)

@Serializable
data class BusEvent(
    val type: String,
    val properties: BusEventProperties? = null
)

@Serializable
data class BusEventProperties(
    @SerialName("sessionID") val sessionId: String? = null,
    val info: Message? = null,
    @SerialName("messageID") val messageId: String? = null,
    val part: MessagePart? = null,
    @SerialName("permissionID") val permissionId: String? = null,
    val permission: PermissionInfo? = null
)

@Serializable
data class PermissionInfo(
    val id: String? = null,
    @SerialName("sessionID") val sessionId: String? = null,
    val request: String? = null,
    val pattern: String? = null,
    val type: String? = null
)

@Serializable
data class PermissionResponse(
    val response: String,
    val remember: Boolean = false
)

@Serializable
data class AbortResponse(
    val aborted: Boolean = true
)
