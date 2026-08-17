package com.opencode.mobile.data.api

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import okhttp3.*
import okio.BufferedSource
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SSE (Server-Sent Events) client for OpenCode event stream.
 * Parses the SSE format: "event: <type>\ndata: <json>\n\n"
 */
@Singleton
class SseEventSource @Inject constructor() {

    private val _events = MutableSharedFlow<SseEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SseEvent> = _events.asSharedFlow()

    private var client: OkHttpClient? = null
    private var call: Call? = null
    private var scope: CoroutineScope? = null

    fun connect(baseUrl: String, password: String? = null) {
        disconnect()

        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        client = OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS) // no timeout for SSE
            .build()

        val url = "${baseUrl.trimEnd('/')}/event"
        val requestBuilder = Request.Builder().url(url).get()

        if (!password.isNullOrBlank()) {
            val credentials = okhttp3.Credentials.basic("opencode", password)
            requestBuilder.header("Authorization", credentials)
        }

        call = client?.newCall(requestBuilder.build())

        scope?.launch {
            try {
                val response = call?.execute()
                if (response?.isSuccessful != true) {
                    Log.e("SSE", "SSE connection failed: ${response?.code}")
                    return@launch
                }

                val body = response.body ?: return@launch
                val source = body.source()
                var currentEvent = ""

                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break

                    when {
                        line.startsWith("event:") -> {
                            currentEvent = line.removePrefix("event:").trim()
                        }
                        line.startsWith("data:") -> {
                            val data = line.removePrefix("data:").trim()
                            if (data.isNotEmpty() && currentEvent.isNotEmpty()) {
                                try {
                                    val json = Json.parseToJsonElement(data)
                                    _events.emit(SseEvent(type = currentEvent, properties = json))
                                } catch (e: Exception) {
                                    Log.w("SSE", "Failed to parse SSE data: $data", e)
                                }
                            }
                            currentEvent = ""
                        }
                        line.isEmpty() -> {
                            // Empty line = end of event
                            currentEvent = ""
                        }
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    Log.e("SSE", "SSE connection error", e)
                }
            }
        }
    }

    fun disconnect() {
        scope?.cancel()
        scope = null
        call?.cancel()
        call = null
        client?.dispatcher?.executorService?.shutdown()
        client = null
    }
}

data class SseEvent(
    val type: String,
    val properties: JsonElement
)
