package com.escapecall.network

import com.escapecall.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Result of a trigger-call API request.
 */
sealed class TriggerResult {
    data class Success(val callSid: String) : TriggerResult()
    data class Error(val message: String) : TriggerResult()
}

/**
 * Lightweight API client. No Retrofit — just one endpoint.
 */
object ApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * POST /api/trigger-call with the secret token header.
     * Runs on IO dispatcher — safe to call from a coroutine.
     */
    suspend fun triggerCall(): TriggerResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${Config.BACKEND_URL}/api/trigger-call")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("X-Escape-Token", Config.SECRET_TOKEN)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                TriggerResult.Success(callSid = response.body?.string() ?: "ok")
            } else {
                TriggerResult.Error("HTTP ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            TriggerResult.Error(e.message ?: "Unknown error")
        }
    }
}
