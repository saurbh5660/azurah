package com.live.azurah.retrofit

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request

fun getPublicIpAddress(): String = runBlocking {
    try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.ipify.org") // returns plain text IP
            .build()
        val response = client.newCall(request).execute()
        val ip = response.body?.string()?.trim()
        ip ?: "0.0.0.0"
    } catch (e: Exception) {
        e.printStackTrace()
        "0.0.0.0"
    }
}
