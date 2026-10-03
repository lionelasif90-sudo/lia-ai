package com.Lia.assistant.voice

import com.Lia.assistant.action.LiaToolCatalog
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Full-duplex Gemini Live over WebSocket. Never logs raw server messages or the API key.
 * Do NOT set realtimeInputConfig.automaticActivityDetection (server VAD stays default).
 */
class GeminiLiveClient(
    private val apiKey: String,
    private val listener: Listener,
) {
    interface Listener {
        fun onSetupComplete()
        fun onAudioChunk(bytes: ByteArray)
        fun onInputTranscript(text: String)
        fun onOutputTranscript(text: String)
        fun onToolCall(id: String, name: String, args: JSONObject)
        fun onTurnComplete()
        fun onInterrupted()
        fun onError(message: String)
        fun onClosed()
    }

    companion object {
        // Keep the Live model name in ONE place. Check the Live API docs for the current native-audio model.
        const val MODEL = "models/gemini-3.1-flash-live-preview"
        private const val WS_BASE =
            "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent"

        internal fun buildSetup(
            model: String, systemInstruction: String, voiceName: String, declarations: JSONArray,
        ): JSONObject {
            val setup = JSONObject()
                .put("model", model)
                .put("generationConfig", JSONObject()
                    .put("responseModalities", JSONArray().put("AUDIO"))
                    .put("speechConfig", JSONObject().put("voiceConfig", JSONObject()
                        .put("prebuiltVoiceConfig", JSONObject().put("voiceName", voiceName)))))
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
                .put("inputAudioTranscription", JSONObject())
                .put("outputAudioTranscription", JSONObject())
            if (declarations.length() > 0) {
                setup.put("tools", JSONArray().put(JSONObject().put("functionDeclarations", declarations)))
            }
            return JSONObject().put("setup", setup)
        }
    }

    private val http = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .connectTimeout(20, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    @Volatile private var ws: WebSocket? = null
    @Volatile private var ready = false
    @Volatile private var closedByUs = false
    val deduplicator = ToolCallDeduplicator()

    fun connect(systemInstruction: String, voiceName: String = "Aoede") {
        closedByUs = false
        val request = Request.Builder().url("$WS_BASE?key=$apiKey").build()
        ws = http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val setup = buildSetup(MODEL, systemInstruction, voiceName, LiaToolCatalog.declarations())
                webSocket.send(setup.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) = handle(text)
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) = handle(bytes.utf8())

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                ready = false
                if (!closedByUs) listener.onError(
                    if (response != null) "Connection failed (HTTP ${response.code})" else "Connection failed"
                )
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                ready = false
                if (!closedByUs) listener.onClosed()
            }
        })
    }

    fun sendAudio(pcm: ByteArray) {
        if (!ready) return
        val data = Base64.getEncoder().encodeToString(pcm)
        val msg = JSONObject().put("realtimeInput", JSONObject().put("audio",
            JSONObject().put("mimeType", "audio/pcm;rate=16000").put("data", data)))
        ws?.send(msg.toString())
    }

    fun sendToolResponse(id: String, name: String, result: JSONObject) {
        val msg = JSONObject().put("toolResponse", JSONObject().put("functionResponses", JSONArray().put(
            JSONObject().put("id", id).put("name", name).put("response", result))))
        ws?.send(msg.toString())
    }

    fun disconnect() {
        closedByUs = true
        ready = false
        runCatching { ws?.close(1000, "bye") }
        ws = null
    }

    private fun handle(raw: String) {
        val root = try { JSONObject(raw) } catch (_: Exception) { return }
        if (root.has("setupComplete")) {
            ready = true
            listener.onSetupComplete()
        }
        root.optJSONObject("toolCall")?.optJSONArray("functionCalls")?.let { calls ->
            for (i in 0 until calls.length()) {
                val c = calls.optJSONObject(i) ?: continue
                val id = c.optString("id")
                val name = c.optString("name")
                if (name.isEmpty()) continue
                if (id.isNotEmpty() && !deduplicator.firstTime(id)) continue
                listener.onToolCall(id, name, c.optJSONObject("args") ?: JSONObject())
            }
        }
        val sc = root.optJSONObject("serverContent")
        if (sc != null) {
            sc.optJSONObject("modelTurn")?.optJSONArray("parts")?.let { parts ->
                for (i in 0 until parts.length()) {
                    val inline = parts.optJSONObject(i)?.optJSONObject("inlineData") ?: continue
                    if (inline.optString("mimeType").startsWith("audio/pcm")) {
                        val b64 = inline.optString("data")
                        if (b64.isNotEmpty()) {
                            runCatching { Base64.getDecoder().decode(b64) }.getOrNull()?.let(listener::onAudioChunk)
                        }
                    }
                }
            }
            sc.optJSONObject("inputTranscription")?.optString("text")?.takeIf { it.isNotEmpty() }
                ?.let(listener::onInputTranscript)
            sc.optJSONObject("outputTranscription")?.optString("text")?.takeIf { it.isNotEmpty() }
                ?.let(listener::onOutputTranscript)
            if (sc.optBoolean("interrupted", false)) listener.onInterrupted()
            if (sc.optBoolean("turnComplete", false)) listener.onTurnComplete()
        }
        // sessionResumptionUpdate and goAway are ignored on purpose.
    }
}
