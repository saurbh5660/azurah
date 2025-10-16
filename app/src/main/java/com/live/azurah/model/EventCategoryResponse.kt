package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class EventCategoryResponse(
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
        @SerializedName("current_page")
        val currentPage: Int? = null,
        @SerializedName("data")
        val data: List<Data>? = null,
        @SerializedName("per_page")
        val perPage: Int? = null,
        @SerializedName("total_count")
        val totalCount: Int? = null,
        @SerializedName("total_pages")
        val totalPages: Int? = null
    ) {
        data class Data(
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("image")
            val image: Any? = null,
            @SerializedName("image_thumb")
            val imageThumb: Any? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("name")
            val name: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null
        )
    }
}