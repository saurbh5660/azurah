package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class DiscussionListResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("body") val body: Body? = null
) {
    data class Body(
        @SerializedName("total_pages") val total_pages: Int? = null,
        @SerializedName("total_count") val total_count: Int? = null,
        @SerializedName("current_page") val current_page: Int? = null,
        @SerializedName("per_page") val per_page: Int? = null,
        @SerializedName("data") val data: List<DiscussionData>? = null
    )

    data class DiscussionData(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("bible_quest_id") val bible_quest_id: Int? = null,
        @SerializedName("bible_quest_challenge_id") val bible_quest_challenge_id: Int? = null,
        @SerializedName("added_by") val added_by: Int? = null,
        @SerializedName("title") val title: String? = null,
        @SerializedName("description") val description: String? = null,
        @SerializedName("like_count") var like_count: Int? = null,
        @SerializedName("is_like") var is_like: Int? = null,
        @SerializedName("comment_count") var comment_count: Int? = null,
        @SerializedName("created_at") val created_at: String? = null,
        @SerializedName("created") val created: Long? = null,
        @SerializedName("user") val user: UserModel? = null
    )
}
