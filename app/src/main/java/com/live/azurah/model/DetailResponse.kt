package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class DetailResponse(
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
        @SerializedName("comment_count")
        var commentCount: Int? = null,
        @SerializedName("praise_count")
        var praiseCount: Int? = null,
        @SerializedName("is_praise")
        var is_praise: Int? = null,
        @SerializedName("created")
        val created: Int? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("deleted_at")
        val deletedAt: Any? = null,
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("is_deleted")
        val isDeleted: String? = null,
        @SerializedName("is_like")
        var isLike: Int? = null,
        @SerializedName("is_reported")
        val isReported: Int? = null,
        @SerializedName("like_count")
        var likeCount: Int? = null,
        @SerializedName("prayer_category")
        val prayerCategory: PrayerCategory? = null,
        @SerializedName("testimony_category")
        val testimonyCategory: PrayerCategory? = null,
        @SerializedName("category")
        val category: PrayerCategory? = null,
        @SerializedName("prayer_category_id")
        val prayerCategoryId: Int? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("title")
        val title: String? = null,
        @SerializedName("type")
        val type: Int? = null,
        @SerializedName("updated")
        val updated: Int? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("user")
        val user: User? = null,
        @SerializedName("user_id")
        val userId: Int? = null
    ) {
        data class PrayerCategory(
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("name")
            val name: String? = null
        )

        data class User(
            @SerializedName("country_code")
            val countryCode: String? = null,
            @SerializedName("email")
            val email: String? = null,
            @SerializedName("first_name")
            val firstName: String? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("image")
            val image: String? = null,
            @SerializedName("image_thumb")
            val imageThumb: String? = null,
            @SerializedName("last_name")
            val lastName: String? = null,
            @SerializedName("phone")
            val phone: String? = null,
            @SerializedName("role")
            val role: String? = null,
            @SerializedName("username")
            val username: String? = null
        )
    }
}