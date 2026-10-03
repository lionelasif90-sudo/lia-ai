package com.Lia.assistant.voice

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ChatTurn(val isUser: Boolean, val text: String)

sealed class ChatReplyResult {
    data class Success(val text: String) : ChatReplyResult()
    data class Error(val message: String) : ChatReplyResult()
}

object GeminiTextClient {
    private const val BASE = "https://generativelanguage.googleapis.com/v1beta/"
    private const val TAG = "LiaText"

    internal val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Volatile private var cachedModel: String? = null

    /** First generateContent model that is a "flash" model and not live/image/tts. Never hardcode ids. */
    internal fun chooseModel(listJson: String): String? {
        val models = try { JSONObject(listJson).optJSONArray("models") } catch (_: Exception) { null } ?: return null
        val generate = (0 until models.length()).mapNotNull { models.optJSONObject(it) }.filter { m ->
            val methods = m.optJSONArray("supportedGenerationMethods")
            methods != null && (0 until methods.length()).any { methods.optString(it) == "generateContent" }
        }
        val flash = generate.firstOrNull { m ->
            val n = m.optString("name").lowercase()
            "flash" in n && "live" !in n && "image" !in n && "tts" !in n
        }
        return (flash ?: generate.firstOrNull())?.optString("name")?.takeIf { it.isNotEmpty() }
    }

    internal suspend fun resolveModel(apiKey: String): String? {
        cachedModel?.let { return it }
        return withContext(Dispatchers.IO) {
            try {
                val req = Request.Builder().url(BASE + "models").header("x-goog-api-key", apiKey).build()
                http.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) { Log.w(TAG, "ListModels HTTP ${resp.code}"); return@use null }
                    chooseModel(resp.body?.string().orEmpty())?.also { cachedModel = it }
                }
            } catch (_: IOException) { null }
        }
    }

    internal fun buildRequest(systemInstruction: String, history: List<ChatTurn>): JSONObject {
        val contents = JSONArray()
        history.forEach { t ->
            contents.put(JSONObject()
                .put("role", if (t.isUser) "user" else "model")
                .put("parts", JSONArray().put(JSONObject().put("text", t.text))))
        }
        return JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))
            .put("contents", contents)
    }

    internal fun parseReply(body: String): ChatReplyResult {
        val root = try { JSONObject(body) } catch (_: Exception) { return ChatReplyResult.Error("Unexpected reply from Gemini.") }
        val text = root.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")
            ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty().trim()
        if (text.isNotEmpty()) return ChatReplyResult.Success(text)
        val blocked = root.optJSONObject("promptFeedback")?.optString("blockReason").orEmpty()
        return ChatReplyResult.Error(
            if (blocked.isNotEmpty()) "Gemini couldn't answer that request." else "Gemini sent an empty reply. Try again."
        )
    }

    suspend fun reply(apiKey: String, systemInstruction: String, history: List<ChatTurn>): ChatReplyResult {
        if (apiKey.isBlank()) return ChatReplyResult.Error("Add your Gemini API key in Settings first.")
        if (history.isEmpty()) return ChatReplyResult.Error("Nothing to reply to.")
        val model = resolveModel(apiKey) ?: return ChatReplyResult.Error("Couldn't reach Gemini to pick a model. Check your key and connection.")
        return withContext(Dispatchers.IO) {
            try {
                val body = buildRequest(systemInstruction, history).toString()
                    .toRequestBody("application/json".toMediaType())
                val req = Request.Builder().url("$BASE$model:generateContent")
                    .header("x-goog-api-key", apiKey).post(body).build()
                http.newCall(req).execute().use { resp ->
                    val text = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        val reason = try { JSONObject(text).optJSONObject("error")?.optString("message").orEmpty() } catch (_: Exception) { "" }
                        Log.w(TAG, "HTTP ${resp.code}: ${reason.take(200)}") // never the key or user text
                        ChatReplyResult.Error(when (resp.code) {
                            400, 403 -> "Gemini rejected the request. Check your API key."
                            429 -> "Gemini is rate limiting you. Try again shortly."
                            503 -> "Gemini is overloaded right now. Try again."
                            else -> "Gemini error (${resp.code})."
                        })
                    } else parseReply(text)
                }
            } catch (_: IOException) {
                ChatReplyResult.Error("Couldn't reach Gemini. Check your internet connection.")
            }
        }
    }
}
