package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class SavedEventResponse(
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
            @SerializedName("created")
            val created: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted")
            val deleted: Int? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("event")
            val event: Event? = null,
            @SerializedName("event_id")
            val eventId: Int? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            @SerializedName("user")
            val user: User? = null,
            @SerializedName("user_id")
            val userId: Int? = null
        ) {
            data class Event(
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("end_date")
                val endDate: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("image_thumb")
                val imageThumb: String? = null,
                @SerializedName("price")
                val price: String? = null,
                @SerializedName("start_date")
                val startDate: String? = null,
                @SerializedName("start_time")
                val startTime: String? = null,
                @SerializedName("end_time")
                val endTime: String? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("location")
                val location: String? = null
            )

            data class User(
                @SerializedName("first_name")
                val firstName: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("image_thumb")
                val imageThumb: String? = null,
                @SerializedName("last_name")
                val lastName: String? = null
            )
        }
    }
}