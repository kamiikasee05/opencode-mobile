package com.opencode.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Message(
    val id: String? = null,
    @SerialName("sessionID") val sessionId: String? = null,
    val role: String,
    val parts: List<MessagePart> = emptyList(),
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("finishedAt") val finishedAt: String? = null,
    val error: String? = null
)

@Serializable
data class MessagePart(
    val type: String,
    val text: String? = null,
    @SerialName("toolInvocation") val toolInvocation: ToolInvocation? = null,
    @SerialName("toolResult") val toolResult: ToolResult? = null
)

@Serializable
data class ToolInvocation(
    val state: String? = null,
    val toolCallId: String? = null,
    val toolName: String? = null,
    val args: JsonElement? = null,
    val result: JsonElement? = null
)

@Serializable
data class ToolResult(
    val toolCallId: String? = null,
    val toolName: String? = null,
    val state: String? = null,
    val result: JsonElement? = null
)

@Serializable
data class SendMessageRequest(
    @SerialName("messageID") val messageId: String? = null,
    val model: ModelRef? = null,
    val agent: String? = null,
    val parts: List<MessagePart>,
    val format: JsonElement? = null
)

@Serializable
data class ModelRef(
    @SerialName("providerID") val providerId: String,
    @SerialName("modelID") val modelId: String
)

@Serializable
data class PromptAsyncRequest(
    @SerialName("messageID") val messageId: String? = null,
    val model: ModelRef? = null,
    val agent: String? = null,
    val parts: List<MessagePart>,
    val format: JsonElement? = null
)
