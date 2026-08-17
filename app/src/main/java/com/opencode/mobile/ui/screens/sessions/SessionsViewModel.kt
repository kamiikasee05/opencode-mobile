package com.opencode.mobile.ui.screens.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.mobile.data.model.Session
import com.opencode.mobile.data.repository.OpenCodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionsUiState(
    val sessions: List<Session> = emptyList(),
    val loading: Boolean = false,
    val connected: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val repository: OpenCodeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsUiState())
    val uiState: StateFlow<SessionsUiState> = _uiState.asStateFlow()

    init {
        refresh()
        connectSse()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                repository.applySettings()
                val sessions = repository.listSessions().getOrThrow()
                _uiState.update {
                    it.copy(sessions = sessions, loading = false, connected = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(loading = false, connected = false, error = e.message)
                }
            }
        }
    }

    fun createSession() {
        viewModelScope.launch {
            try {
                val session = repository.createSession().getOrThrow()
                _uiState.update {
                    it.copy(sessions = listOf(session) + it.sessions)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSession(id)
                _uiState.update {
                    it.copy(sessions = it.sessions.filter { s -> s.id != id })
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun connectSse() {
        repository.connectSse()
    }
}
