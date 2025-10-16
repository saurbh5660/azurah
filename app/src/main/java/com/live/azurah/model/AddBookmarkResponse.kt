package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class AddBookmarkResponse(
    @SerializedName("body")
    val body: Body? = null,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("success")
    val success: Boolean? = null
) {
    data class Body(
        @SerializedName("isBooking")
        val isBooking: Int? = null,
        @SerializedName("message")
        val message: String? = null
    )
}