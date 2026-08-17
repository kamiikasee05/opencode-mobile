package com.opencode.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ProviderListResponse(
    val all: List<Provider>? = null,
    val connected: List<String>? = null,
    val default: Map<String, JsonElement>? = null
)

@Serializable
data class Provider(
    val id: String,
    val name: String? = null,
    val api: String? = null,
    val npm: String? = null,
    val options: Map<String, JsonElement>? = null,
    val models: Map<String, Model>? = null
)

@Serializable
data class Model(
    val id: String,
    val name: String? = null,
    val family: String? = null,
    @SerialName("providerID") val providerId: String? = null,
    val reasoning: Boolean? = null,
    val attachment: Boolean? = null,
    val cost: ModelCost? = null
)

@Serializable
data class ModelCost(
    val input: Double? = null,
    val output: Double? = null
)

@Serializable
data class AgentListResponse(
    val all: List<Agent>? = null,
    val connected: List<String>? = null,
    val default: Map<String, JsonElement>? = null
)

@Serializable
data class Agent(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val mode: String? = null,
    val native: Boolean = false,
    val options: Map<String, JsonElement>? = null
) {
    /** Agent display ID — server may send "id" or "name" */
    val displayId: String get() = id ?: name ?: "unknown"
}

@Serializable
data class Config(
    val model: ModelRef? = null,
    val agent: String? = null
)

@Serializable
data class HealthResponse(
    val healthy: Boolean? = null,
    val version: String? = null,
    val status: String? = null
)
