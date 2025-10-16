package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class EventResponse(
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
        @SerializedName("getData")
        val getData: GetData? = null
    ) {
        data class GetData(
            @SerializedName("current_page")
            val currentPage: Int? = null,
            @SerializedName("data")
            val `data`: List<Data?>? = null,
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
                @SerializedName("deleted_at")
                val deletedAt: Any? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("end_date")
                val endDate: String? = null,
                @SerializedName("event_category")
                val eventCategory: EventCategory? = null,
                @SerializedName("event_category_id")
                val eventCategoryId: Int? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("image_thumb")
                val imageThumb: String? = null,
                @SerializedName("is_bookmark")
                val isBookmark: Int? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("is_expired")
                val isExpired: String? = null,
                @SerializedName("latitude")
                val latitude: String? = null,
                @SerializedName("location")
                val location: String? = null,
                @SerializedName("longitude")
                val longitude: String? = null,
                @SerializedName("price")
                val price: String? = null,
                @SerializedName("start_date")
                val startDate: String? = null,
                @SerializedName("start_time")
                val startTime: String? = null,
                @SerializedName("end_time")
                val endTime: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null,
                @SerializedName("website_url")
                val websiteUrl: String? = null
            ) {
                data class EventCategory(
                    @SerializedName("id")
                    val id: Int? = null,
                    @SerializedName("name")
                    val name: String? = null
                )
            }
        }
    }
}