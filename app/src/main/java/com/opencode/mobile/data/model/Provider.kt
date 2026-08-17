package com.opencode.mobile.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val id: String,
    val name: String? = null,
    @SerialName("isConnected") val isConnected: Boolean = false,
    val models: List<Model> = emptyList()
)

@Serializable
data class Model(
    val id: String,
    val name: String? = null,
    val provider: ProviderRef? = null
)

@Serializable
data class ProviderRef(
    val id: String? = null
)

@Serializable
data class Agent(
    val id: String,
    val name: String? = null,
    val description: String? = null
)

@Serializable
data class Config(
    val model: ModelRef? = null,
    val agent: String? = null,
    @SerialName("provider") val providers: Map<String, JsonElement>? = null
)

@Serializable
data class HealthResponse(
    val version: String? = null,
    val status: String? = null
)
