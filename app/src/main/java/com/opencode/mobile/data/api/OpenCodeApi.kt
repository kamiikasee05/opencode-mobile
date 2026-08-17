package com.opencode.mobile.data.api

import io.ktor.client.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCodeApi @Inject constructor(
    private val client: HttpClient
) {
    // -- Health --
    suspend fun health(): com.opencode.mobile.data.model.HealthResponse =
        client.get("global/health").body()

    // -- Sessions --
    suspend fun listSessions(): List<com.opencode.mobile.data.model.Session> =
        client.get("session").body()

    suspend fun createSession(request: com.opencode.mobile.data.model.CreateSessionRequest = com.opencode.mobile.data.model.CreateSessionRequest()): com.opencode.mobile.data.model.Session =
        client.post("session") {
            setBody(request)
        }.body()

    suspend fun getSession(id: String): com.opencode.mobile.data.model.Session =
        client.get("session/$id").body()

    suspend fun deleteSession(id: String) {
        client.delete("session/$id")
    }

    suspend fun updateSession(id: String, request: com.opencode.mobile.data.model.UpdateSessionRequest): com.opencode.mobile.data.model.Session =
        client.patch("session/$id") {
            setBody(request)
        }.body()

    suspend fun forkSession(id: String): com.opencode.mobile.data.model.Session =
        client.post("session/$id/fork").body()

    suspend fun abortSession(id: String): com.opencode.mobile.data.model.AbortResponse =
        client.post("session/$id/abort").body()

    suspend fun shareSession(id: String): String =
        client.post("session/$id/share").body()

    // -- Messages --
    suspend fun listMessages(sessionId: String): List<com.opencode.mobile.data.model.Message> =
        client.get("session/$sessionId/message").body()

    suspend fun sendMessage(sessionId: String, request: com.opencode.mobile.data.model.SendMessageRequest): com.opencode.mobile.data.model.Message =
        client.post("session/$sessionId/message") {
            setBody(request)
        }.body()

    suspend fun sendPromptAsync(sessionId: String, request: com.opencode.mobile.data.model.PromptAsyncRequest) {
        client.post("session/$sessionId/prompt_async") {
            setBody(request)
        }
    }

    // -- Permissions --
    suspend fun respondPermission(sessionId: String, permissionId: String, response: com.opencode.mobile.data.model.PermissionResponse) {
        client.post("session/$sessionId/permissions/$permissionId") {
            setBody(response)
        }
    }

    // -- Providers & Agents --
    suspend fun listProviders(): List<com.opencode.mobile.data.model.Provider> =
        client.get("provider").body()

    suspend fun listAgents(): List<com.opencode.mobile.data.model.Agent> =
        client.get("agent").body()

    // -- Config --
    suspend fun getConfig(): com.opencode.mobile.data.model.Config =
        client.get("config").body()

    suspend fun updateConfig(config: com.opencode.mobile.data.model.Config): com.opencode.mobile.data.model.Config =
        client.patch("config") {
            setBody(config)
        }.body()
}

// Extension functions for typed requests
private suspend inline fun <reified T> io.ktor.client.HttpClient.get(
    urlString: String,
    block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}
): T = io.ktor.client.request.get(urlString, block).body()

private suspend inline fun <reified T> io.ktor.client.HttpClient.post(
    urlString: String,
    block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}
): T = io.ktor.client.request.post(urlString, block).body()

private suspend inline fun <reified T> io.ktor.client.HttpClient.patch(
    urlString: String,
    block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}
): T = io.ktor.client.request.patch(urlString, block).body()

private suspend fun io.ktor.client.HttpClient.delete(urlString: String) {
    io.ktor.client.request.delete(urlString)
}
