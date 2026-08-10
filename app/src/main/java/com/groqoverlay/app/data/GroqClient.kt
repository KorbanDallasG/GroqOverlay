package com.groqoverlay.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GroqClient {
    private const val URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODELS_URL = "https://api.groq.com/openai/v1/models"

    val client: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()

    fun buildStreamCall(
        key: String,
        model: String,
        systemPrompt: String,
        history: List<Message>
    ): Call {
        val messages = JSONArray()
        if (systemPrompt.isNotBlank()) {
            messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
}
        history.takeLast(20).forEach {
            messages.put(JSONObject().put("role", it.role).put("content", it.content))
}
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.7)
            .put("max_tokens", 1024)
            .put("stream", true)
            .toString()
        val request = Request.Builder()
            .url(URL)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        return client.newCall(request)
    }

    suspend fun readStream(call: Call, onToken: (String) -> Unit): String =
        withContext(Dispatchers.IO) {
        val sb = StringBuilder()
            call.execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}: ${errorBody(response)}")
        val source = response.body?.source() ?: throw IOException("Пустой ответ")
                while (!source.exhausted()) {
        val line = source.readUtf8Line() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
        if (data == "[DONE]") break
                    try {
        val delta = JSONObject(data)
            .getJSONArray("choices").getJSONObject(0)
            .optJSONObject("delta") ?: continue
                        val token = delta.optString("content", "")
        if (token.isNotEmpty()) {
                            sb.append(token)
                            onToken(token)
}
} catch (e: Exception) { }
}
}
            sb.toString()
}

    suspend fun testKey(key: String): String = withContext(Dispatchers.IO) {
        try {
        val request = Request.Builder()
            .url(MODELS_URL)
            .addHeader("Authorization", "Bearer $key")
            .build()
            client.newCall(request).execute().use { r ->
                if (r.isSuccessful) "✅ Ключ действителен"
                else "❌ HTTP ${r.code}: ${errorBody(r)}"
            }
} catch (e: Exception) {
            "❌ Ошибка сети: ${e.message}"
        }
    }

    private fun errorBody(response: Response): String = try {
        val body = response.body?.string().orEmpty()
        val json = JSONObject(body)
        if (json.has("error")) {
        val e = json.get("error")
        if (e is JSONObject) e.optString("message") else e.toString()
} else body.take(200)
    } catch (_: Exception) { response.message.take(200) }
}
