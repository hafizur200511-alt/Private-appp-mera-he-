package com.example.data

import com.example.model.ApiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object ApiProviderClient {

    suspend fun testConnection(provider: ApiProvider): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (provider.baseUrl.isBlank()) return@withContext Result.failure(Exception("Provider API URL cannot be empty"))
            if (provider.apiKey.isBlank()) return@withContext Result.failure(Exception("Provider API key cannot be empty"))

            val params = mapOf(
                "key" to provider.apiKey,
                "action" to "balance"
            )

            val response = postForm(provider.baseUrl, params)
            val json = JSONObject(response)
            if (json.has("error")) {
                Result.failure(Exception("Provider Error: ${json.optString("error")}"))
            } else {
                val balance = json.optString("balance", "N/A")
                val currency = json.optString("currency", "INR")
                Result.success("Connection Success! Balance: $currency $balance")
            }
        } catch (e: Exception) {
            Result.failure(Exception("Connection failed: ${e.message}"))
        }
    }

    suspend fun submitOrder(
        provider: ApiProvider,
        serviceId: String,
        link: String,
        quantity: Long
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val params = mapOf(
                "key" to provider.apiKey,
                "action" to "add",
                "service" to serviceId,
                "link" to link,
                "quantity" to quantity.toString()
            )

            val response = postForm(provider.baseUrl, params)
            val json = JSONObject(response)
            if (json.has("order")) {
                val providerOrderId = json.get("order").toString()
                Result.success(providerOrderId)
            } else if (json.has("error")) {
                Result.failure(Exception("Provider order rejected: ${json.optString("error")}"))
            } else {
                Result.failure(Exception("Unknown response from provider: $response"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reach provider: ${e.message}"))
        }
    }

    private fun postForm(urlStr: String, params: Map<String, String>): String {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.doInput = true
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("User-Agent", "MEGA-SMM-INDIA-Agent/1.0")

        val bodyBuilder = StringBuilder()
        var first = true
        for ((key, value) in params) {
            if (first) first = false else bodyBuilder.append("&")
            bodyBuilder.append(URLEncoder.encode(key, "UTF-8"))
            bodyBuilder.append("=")
            bodyBuilder.append(URLEncoder.encode(value, "UTF-8"))
        }

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(bodyBuilder.toString())
            writer.flush()
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream ?: conn.inputStream
        val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
        conn.disconnect()
        return response
    }
}
