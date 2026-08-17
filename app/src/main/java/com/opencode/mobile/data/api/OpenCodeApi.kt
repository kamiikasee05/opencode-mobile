package com.opencode.mobile.data.api

import com.opencode.mobile.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenCodeApi @Inject constructor(
    private val client: HttpClient
) {
    // -- Health --
    suspend fun health(): HealthResponse =
        client.get("global/health").body()

    // -- Sessions --
    suspend fun listSessions(): List<Session> =
        client.get("session").body()

    suspend fun createSession(request: CreateSessionRequest = CreateSessionRequest()): Session =
        client.post("session") {
            setBody(request)
        }.body()

    suspend fun getSession(id: String): Session =
        client.get("session/$id").body()

    suspend fun deleteSession(id: String) {
        client.delete("session/$id")
    }

    suspend fun updateSession(id: String, request: UpdateSessionRequest): Session =
        client.patch("session/$id") {
            setBody(request)
        }.body()

    suspend fun forkSession(id: String): Session =
        client.post("session/$id/fork").body()

    suspend fun abortSession(id: String): AbortResponse =
        client.post("session/$id/abort").body()

    suspend fun shareSession(id: String): String =
        client.post("session/$id/share").body()

    // -- Messages --
    suspend fun listMessages(sessionId: String): List<Message> =
        client.get("session/$sessionId/message").body()

    suspend fun sendMessage(sessionId: String, request: SendMessageRequest): Message =
        client.post("session/$sessionId/message") {
            setBody(request)
        }.body()

    suspend fun sendPromptAsync(sessionId: String, request: PromptAsyncRequest) {
        client.post("session/$sessionId/prompt_async") {
            setBody(request)
        }
    }

    // -- Permissions --
    suspend fun respondPermission(sessionId: String, permissionId: String, response: PermissionResponse) {
        client.post("session/$sessionId/permissions/$permissionId") {
            setBody(response)
        }
    }

    // -- Providers & Agents --
    suspend fun listProviders(): List<Provider> =
        client.get("provider").body()

    suspend fun listAgents(): List<Agent> =
        client.get("agent").body()

    // -- Config --
    suspend fun getConfig(): Config =
        client.get("config").body()

    suspend fun updateConfig(config: Config): Config =
        client.patch("config") {
            setBody(config)
        }.body()
}
