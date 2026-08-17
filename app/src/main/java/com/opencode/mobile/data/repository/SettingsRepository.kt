package com.opencode.mobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "opencode_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val SERVER_URL = stringPreferencesKey("server_url")
        val PASSWORD = stringPreferencesKey("password")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val SELECTED_PROVIDER = stringPreferencesKey("selected_provider")
        val SELECTED_AGENT = stringPreferencesKey("selected_agent")
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SERVER_URL] ?: "http://192.168.18.11:4096"
    }

    val password: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PASSWORD] ?: ""
    }

    val selectedProvider: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_PROVIDER] ?: ""
    }

    val selectedModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_MODEL] ?: ""
    }

    val selectedAgent: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SELECTED_AGENT] ?: "build"
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[SERVER_URL] = url }
    }

    suspend fun setPassword(pass: String) {
        context.dataStore.edit { it[PASSWORD] = pass }
    }

    suspend fun setSelectedProvider(providerId: String) {
        context.dataStore.edit { it[SELECTED_PROVIDER] = providerId }
    }

    suspend fun setSelectedModel(modelId: String) {
        context.dataStore.edit { it[SELECTED_MODEL] = modelId }
    }

    suspend fun setSelectedAgent(agentId: String) {
        context.dataStore.edit { it[SELECTED_AGENT] = agentId }
    }
}
