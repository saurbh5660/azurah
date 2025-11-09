package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class CountResponse(
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
        @SerializedName("eventBookmarksCount")
        val eventBookmarksCount: Int? = null,
        @SerializedName("favouriteProductsCount")
        var favouriteProductsCount: Int? = null,
        @SerializedName("notificationUnreadCount")
        val notificationUnreadCount: Int? = null
    )
}