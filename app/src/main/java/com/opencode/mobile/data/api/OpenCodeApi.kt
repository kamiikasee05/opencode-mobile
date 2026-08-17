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
    /** Base URL for all requests, e.g. "http://192.168.18.11:4096". Set by OpenCodeRepository.applySettings(). */
    var baseUrl: String = ""

    private fun url(path: String): String {
        val base = baseUrl.trimEnd('/')
        return "$base/$path"
    }

    // -- Health --
    suspend fun health(): HealthResponse =
        client.get(url("global/health")).body()

    // -- Sessions --
    suspend fun listSessions(): List<Session> =
        client.get(url("session")).body()

    suspend fun createSession(request: CreateSessionRequest = CreateSessionRequest()): Session =
        client.post(url("session")) {
            setBody(request)
        }.body()

    suspend fun getSession(id: String): Session =
        client.get(url("session/$id")).body()

    suspend fun deleteSession(id: String) {
        client.delete(url("session/$id"))
    }

    suspend fun updateSession(id: String, request: UpdateSessionRequest): Session =
        client.patch(url("session/$id")) {
            setBody(request)
        }.body()

    suspend fun forkSession(id: String): Session =
        client.post(url("session/$id/fork")).body()

    suspend fun abortSession(id: String): AbortResponse =
        client.post(url("session/$id/abort")).body()

    suspend fun shareSession(id: String): String =
        client.post(url("session/$id/share")).body()

    // -- Messages --
    suspend fun listMessages(sessionId: String): List<Message> =
        client.get(url("session/$sessionId/message")).body()

    suspend fun sendMessage(sessionId: String, request: SendMessageRequest): Message =
        client.post(url("session/$sessionId/message")) {
            setBody(request)
        }.body()

    suspend fun sendPromptAsync(sessionId: String, request: PromptAsyncRequest) {
        client.post(url("session/$sessionId/prompt_async")) {
            setBody(request)
        }
    }

    // -- Permissions --
    suspend fun respondPermission(sessionId: String, permissionId: String, response: PermissionResponse) {
        client.post(url("session/$sessionId/permissions/$permissionId")) {
            setBody(response)
        }
    }

    // -- Providers & Agents --
    suspend fun listProviders(): ProviderListResponse =
        client.get(url("provider")).body()

    suspend fun listAgents(): List<Agent> =
        client.get(url("agent")).body()

    // -- Config --
    suspend fun getConfig(): Config =
        client.get(url("config")).body()

    suspend fun updateConfig(config: Config): Config =
        client.patch(url("config")) {
            setBody(config)
        }.body()
}
