package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class QuestionResponse(
    @SerializedName("body")
    val body: List<Body>? = null,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("success")
    val success: Boolean? = null
) {
    data class Body(
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("deleted_at")
        val deletedAt: Any? = null,
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("is_deleted")
        val isDeleted: String? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null
    )
}