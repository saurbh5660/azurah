package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class CheckUsernameResponse(
    @SerializedName("body")
    val body: List<String>? = null,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("success")
    val success: Boolean? = null
)