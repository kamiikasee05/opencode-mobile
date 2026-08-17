package com.opencode.mobile.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.mobile.data.model.*
import com.opencode.mobile.data.repository.OpenCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import javax.inject.Inject

data class PendingPermission(
    val permissionId: String,
    val sessionId: String,
    val request: String,
    val pattern: String? = null,
    val type: String? = null
)

data class ChatUiState(
    val sessionTitle: String? = null,
    val messages: List<Message> = emptyList(),
    val isStreaming: Boolean = false,
    val error: String? = null,
    val pendingPermission: PendingPermission? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OpenCodeRepository
) : ViewModel() {

    private val sessionId: String = savedStateHandle["sessionId"] ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        collectSseEvents()
    }

    fun loadSession(id: String) {
        viewModelScope.launch {
            try {
                repository.applySettings()
                val session = repository.listMessages(id).getOrThrow()
                _uiState.update { it.copy(messages = session) }

                // Get session title
                val sessions = repository.listSessions().getOrDefault(emptyList())
                val title = sessions.find { it.id == id }?.title
                _uiState.update { it.copy(sessionTitle = title ?: "Chat") }

                // Connect SSE for real-time updates
                repository.connectSse()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Add user message to UI immediately
            val userMessage = Message(
                id = "user-${System.currentTimeMillis()}",
                role = "user",
                parts = listOf(MessagePart(type = "text", text = text))
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + userMessage,
                    isStreaming = true,
                    error = null
                )
            }

            try {
                repository.sendPromptAsync(sessionId, text)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isStreaming = false,
                        error = "Failed to send: ${e.message}"
                    )
                }
            }
        }
    }

    fun abort() {
        viewModelScope.launch {
            try {
                repository.abortSession(sessionId)
                _uiState.update { it.copy(isStreaming = false) }
            } catch (_: Exception) {}
        }
    }

    fun respondPermission(permissionId: String, allow: Boolean, remember: Boolean) {
        viewModelScope.launch {
            try {
                repository.respondPermission(sessionId, permissionId, allow, remember)
                _uiState.update { it.copy(pendingPermission = null) }
            } catch (_: Exception) {}
        }
    }

    private fun collectSseEvents() {
        viewModelScope.launch {
            repository.sseEvents.collect { event ->
                when (event.type) {
                    "message.updated" -> handleMessagesUpdated(event.properties)
                    "message.completed" -> handleMessagesCompleted(event.properties)
                    "message.part.updated" -> handlePartUpdated(event.properties)
                    "permission.asked" -> handlePermissionAsked(event.properties)
                }
            }
        }
    }

    private fun handleMessagesUpdated(properties: JsonElement) {
        try {
            val sessionIdProp = properties.jsonObject["properties"]?.jsonObject?.get("sessionID")?.jsonPrimitive?.contentOrNull
            if (sessionIdProp != sessionId) return

            val info = properties.jsonObject["properties"]?.jsonObject?.get("info")
            if (info != null) {
                val message = Json.decodeFromJsonElement<Message>(info)
                _uiState.update { state ->
                    val existing = state.messages.toMutableList()
                    val idx = existing.indexOfLast { it.id == message.id }
                    if (idx >= 0) {
                        existing[idx] = message
                    } else {
                        existing.add(message)
                    }
                    state.copy(messages = existing)
                }
            }
        } catch (_: Exception) {}
    }

    private fun handleMessagesCompleted(properties: JsonElement) {
        _uiState.update { it.copy(isStreaming = false) }
    }

    private fun handlePartUpdated(properties: JsonElement) {
        try {
            val props = properties.jsonObject["properties"]?.jsonObject ?: return
            val sessionIdProp = props["sessionID"]?.jsonPrimitive?.contentOrNull
            if (sessionIdProp != sessionId) return

            val messageId = props["messageID"]?.jsonPrimitive?.contentOrNull ?: return
            val partElement = props["part"]
            if (partElement == null) return

            val part = Json.decodeFromJsonElement<MessagePart>(partElement)

            _uiState.update { state ->
                val existing = state.messages.toMutableList()
                val msgIdx = existing.indexOfLast { it.id == messageId }

                if (msgIdx >= 0) {
                    val msg = existing[msgIdx]
                    val parts = msg.parts.toMutableList()
                    val partIdx = parts.indexOfLast { it.type == part.type }

                    if (partIdx >= 0) {
                        parts[partIdx] = part
                    } else {
                        parts.add(part)
                    }
                    existing[msgIdx] = msg.copy(parts = parts)
                } else {
                    // Create new message with this part
                    existing.add(Message(
                        id = messageId,
                        role = "assistant",
                        parts = listOf(part)
                    ))
                }

                state.copy(messages = existing, isStreaming = true)
            }
        } catch (_: Exception) {}
    }

    private fun handlePermissionAsked(properties: JsonElement) {
        try {
            val props = properties.jsonObject["properties"]?.jsonObject ?: return
            val sessionIdProp = props["sessionID"]?.jsonPrimitive?.contentOrNull
            if (sessionIdProp != sessionId) return

            val permission = PendingPermission(
                permissionId = props["permissionID"]?.jsonPrimitive?.contentOrNull ?: return,
                sessionId = sessionId,
                request = props["request"]?.jsonPrimitive?.contentOrNull ?: "Unknown request",
                pattern = props["pattern"]?.jsonPrimitive?.contentOrNull,
                type = props["type"]?.jsonPrimitive?.contentOrNull
            )

            _uiState.update { it.copy(pendingPermission = permission) }
        } catch (_: Exception) {}
    }
}
