package com.opencode.mobile.data.repository

import com.opencode.mobile.data.api.OpenCodeApi
import com.opencode.mobile.data.api.SseEventSource
import com.opencode.mobile.data.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCodeRepository @Inject constructor(
    private val api: OpenCodeApi,
    private val sseSource: SseEventSource,
    private val settings: SettingsRepository,
    private val httpClient: HttpClient
) {
    private var currentBaseUrl: String = ""
    private var currentPassword: String = ""

    /** Apply stored settings to the HTTP client. */
    suspend fun applySettings() {
        val url = settings.serverUrl.first()
        val pass = settings.password.first()
        currentBaseUrl = url
        currentPassword = pass

        // Set the base URL on the API so all requests use absolute URLs
        api.baseUrl = url
    }

    fun connectSse() {
        if (currentBaseUrl.isNotBlank()) {
            val baseUrl = currentBaseUrl.trimEnd('/')
            sseSource.connect(baseUrl, currentPassword.ifBlank { null })
        }
    }

    fun disconnectSse() {
        sseSource.disconnect()
    }

    val sseEvents = sseSource.events

    // -- Health --
    suspend fun health(): Result<HealthResponse> = runCatching {
        api.health()
    }

    // -- Sessions --
    suspend fun listSessions(): Result<List<Session>> = runCatching {
        api.listSessions()
    }

    suspend fun createSession(title: String? = null): Result<Session> = runCatching {
        api.createSession(CreateSessionRequest(title = title))
    }

    suspend fun deleteSession(id: String): Result<Unit> = runCatching {
        api.deleteSession(id)
    }

    suspend fun abortSession(id: String): Result<AbortResponse> = runCatching {
        api.abortSession(id)
    }

    // -- Messages --
    suspend fun listMessages(sessionId: String): Result<List<Message>> = runCatching {
        api.listMessages(sessionId)
    }

    suspend fun sendMessage(sessionId: String, text: String, model: ModelRef? = null, agent: String? = null): Result<Message> = runCatching {
        val parts = listOf(MessagePart(type = "text", text = text))
        val providerId = settings.selectedProvider.first()
        val modelId = settings.selectedModel.first()
        val resolvedModel = model ?: if (providerId.isNotBlank() && modelId.isNotBlank()) {
            ModelRef(providerId = providerId, modelId = modelId)
        } else null
        val resolvedAgent = agent ?: settings.selectedAgent.first().ifBlank { null }
        api.sendMessage(sessionId, SendMessageRequest(model = resolvedModel, agent = resolvedAgent, parts = parts))
    }

    suspend fun sendPromptAsync(sessionId: String, text: String, model: ModelRef? = null, agent: String? = null) {
        val parts = listOf(MessagePart(type = "text", text = text))
        val providerId = settings.selectedProvider.first()
        val modelId = settings.selectedModel.first()
        val resolvedModel = model ?: if (providerId.isNotBlank() && modelId.isNotBlank()) {
            ModelRef(providerId = providerId, modelId = modelId)
        } else null
        val resolvedAgent = agent ?: settings.selectedAgent.first().ifBlank { null }
        api.sendPromptAsync(sessionId, PromptAsyncRequest(model = resolvedModel, agent = resolvedAgent, parts = parts))
    }

    // -- Permissions --
    suspend fun respondPermission(sessionId: String, permissionId: String, allow: Boolean, remember: Boolean = false) {
        api.respondPermission(sessionId, permissionId, PermissionResponse(
            response = if (allow) "allow" else "deny",
            remember = remember
        ))
    }

    // -- Providers & Agents --
    suspend fun listProviders(): Result<ProviderListResponse> = runCatching {
        api.listProviders()
    }

    suspend fun listAgents(): Result<List<Agent>> = runCatching {
        api.listAgents()
    }

    // -- Config --
    suspend fun getConfig(): Result<Config> = runCatching {
        api.getConfig()
    }
}
