package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class MyChallengeResponse(
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
        val data: ArrayList<Data>? = null,
        @SerializedName("per_page")
        val perPage: Int? = null,
        @SerializedName("total_count")
        val totalCount: Int? = null,
        @SerializedName("total_pages")
        val totalPages: Int? = null
    ) {
        data class Data(
            @SerializedName("bible_quest")
            val bibleQuest: BibleQuest? = null,
            @SerializedName("bible_quest_challenge_id")
            val bibleQuestChallengeId: Int? = null,
            @SerializedName("bible_quest_id")
            val bibleQuestId: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("date")
            val date: String? = null,
            @SerializedName("date_time")
            val dateTime: Int? = null,
            @SerializedName("day_no")
            val dayNo: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("time")
            val time: String? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            @SerializedName("user")
            val user: User? = null,
            @SerializedName("user_id")
            val userId: Int? = null
        ) {
            data class BibleQuest(
                @SerializedName("advice")
                val advice: String? = null,
                @SerializedName("bible_quest_category_id")
                val bibleQuestCategoryId: Int? = null,
                @SerializedName("bible_verse")
                val bibleVerse: String? = null,
                @SerializedName("bible_version")
                val bibleVersion: String? = null,
                @SerializedName("cover_image")
                val coverImage: String? = null,
                @SerializedName("cover_image_thumb")
                val coverImageThumb: String? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("is_premium")
                val isPremium: String? = null,
                @SerializedName("short_description")
                val shortDescription: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("title")
                val title: String? = null
            )

            data class User(
                @SerializedName("email")
                val email: String? = null,
                @SerializedName("first_name")
                val firstName: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("last_name")
                val lastName: String? = null,
                @SerializedName("username")
                val username: String? = null
            )
        }
    }
}