package com.opencode.mobile.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opencode.mobile.data.model.Agent
import com.opencode.mobile.data.model.Model
import com.opencode.mobile.data.model.Provider
import com.opencode.mobile.data.repository.OpenCodeRepository
import com.opencode.mobile.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val serverUrl: String = "http://192.168.18.11:4096",
    val password: String = "",
    val connected: Boolean = false,
    val saving: Boolean = false,
    val providers: List<Provider> = emptyList(),
    val agents: List<Agent> = emptyList(),
    val selectedProvider: String = "",
    val selectedProviderName: String = "",
    val selectedModel: String = "",
    val selectedModelName: String = "",
    val selectedAgent: String = "build",
    val availableModels: List<Model> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: OpenCodeRepository,
    private val settings: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(settings.serverUrl, settings.password) { url, pass ->
                Pair(url, pass)
            }.collect { (url, pass) ->
                _uiState.update { it.copy(serverUrl = url, password = pass) }
            }
        }

        viewModelScope.launch {
            settings.selectedProvider.collect { providerId ->
                _uiState.update { it.copy(selectedProvider = providerId) }
                updateProviderName()
            }
        }

        viewModelScope.launch {
            settings.selectedModel.collect { modelId ->
                _uiState.update { it.copy(selectedModel = modelId) }
                updateModelName()
            }
        }

        viewModelScope.launch {
            settings.selectedAgent.collect { agentId ->
                _uiState.update { it.copy(selectedAgent = agentId.ifBlank { "build" }) }
            }
        }

        // Try connecting on init
        viewModelScope.launch {
            try {
                repository.applySettings()
                val health = repository.health()
                if (health.isSuccess) {
                    _uiState.update { it.copy(connected = true) }
                    loadProvidersAndAgents()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(connected = false, error = "Connection failed: ${e.message}") }
            }
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.update { it.copy(serverUrl = url) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun selectProvider(providerId: String) {
        viewModelScope.launch {
            settings.setSelectedProvider(providerId)
            _uiState.update { it.copy(selectedProvider = providerId) }
            updateProviderName()
            updateAvailableModels()
        }
    }

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            settings.setSelectedModel(modelId)
            _uiState.update { it.copy(selectedModel = modelId) }
            updateModelName()
        }
    }

    fun selectAgent(agentId: String) {
        viewModelScope.launch {
            settings.setSelectedAgent(agentId)
            _uiState.update { it.copy(selectedAgent = agentId) }
        }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            try {
                settings.setServerUrl(_uiState.value.serverUrl)
                settings.setPassword(_uiState.value.password)
                repository.applySettings()

                val health = repository.health()
                _uiState.update { it.copy(connected = health.isSuccess) }

                if (health.isSuccess) {
                    loadProvidersAndAgents()
                }
                _uiState.update { it.copy(saving = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        saving = false,
                        connected = false,
                        error = "Connection failed: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadProvidersAndAgents() {
        try {
            val providerResponse = repository.listProviders().getOrNull()
            val providers = providerResponse?.all ?: emptyList()
            val agentResponse = repository.listAgents().getOrNull()
            val agents = agentResponse?.all ?: emptyList()
            _uiState.update {
                it.copy(
                    providers = providers,
                    agents = agents
                )
            }
            updateAvailableModels()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to load providers: ${e.message}") }
        }
    }

    private fun updateAvailableModels() {
        val selectedProviderId = _uiState.value.selectedProvider
        val provider = _uiState.value.providers.find { it.id == selectedProviderId }
        _uiState.update {
            it.copy(availableModels = provider?.models?.values?.toList() ?: emptyList())
        }
    }

    private fun updateProviderName() {
        val providerId = _uiState.value.selectedProvider
        val name = _uiState.value.providers.find { it.id == providerId }?.name ?: providerId
        _uiState.update { it.copy(selectedProviderName = name) }
    }

    private fun updateModelName() {
        val modelId = _uiState.value.selectedModel
        val name = _uiState.value.availableModels.find { it.id == modelId }?.name ?: modelId
        _uiState.update { it.copy(selectedModelName = name) }
    }
}
