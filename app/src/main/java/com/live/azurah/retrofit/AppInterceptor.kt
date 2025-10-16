package com.live.azurah.retrofit

import android.content.Context
import android.util.Log
import com.live.azurah.util.getPreference
import okhttp3.Interceptor
import okhttp3.Response

class AppInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val token: String = "Bearer " + getPreference("token","")
        val ipAddress = getPublicIpAddress()
        Log.d("AppInterceptor", "Token: $token")
        Log.d("AppInterceptor", "IP Address: $ipAddress")

        val headers = if (token.isNotEmpty()) {
            request.headers.newBuilder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("Authorization", token)
                .add("location", "hhhhhh")
                .add("latitude", "0.0")
                .add("longitude", "0.0")
                .add("locale", "en")
                .add("user_timezone", "Asia/Kolkata")
                .add("device_id", "87778")
                .add("device_name", "Android")
                .add("ip", ipAddress)
                .add("secret_key", ApiConstants.SECRET_KEY)
                .add("publish_key", ApiConstants.PUBLISH_KEY)
                .build()
        } else {
            request.headers.newBuilder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
                .add("secret_key", ApiConstants.SECRET_KEY)
                .add("publish_key", ApiConstants.PUBLISH_KEY)
                .add("location", "hhhhhh")
                .add("latitude", "0.0")
                .add("longitude", "0.0")
                .add("locale", "en")
                .add("user_timezone", "Asia/Kolkata")
                .add("device_id", "87778")
                .add("device_name", "Android")
                .add("ip", ipAddress)
                .build()
        }
        request = request.newBuilder().headers(headers).build()
        return chain.proceed(request)
    }
}