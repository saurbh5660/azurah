package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class DiscussionCommentItem(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("bible_quest_id") val bible_quest_id: Int? = null,
    @SerializedName("bible_quest_challenge_discussion_id") val bible_quest_challenge_discussion_id: Int? = null,
    @SerializedName("user_id") val user_id: Int? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("like_count") var like_count: Int? = null,
    @SerializedName("is_like") var is_like: Int? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("user") val user: UserModel? = null,
    @SerializedName(value = "discussion_comment_tags", alternate = ["post_comment_tags"])
    val discussion_comment_tags: ArrayList<CommentResponse.PostCommentTag> = ArrayList(),
    @SerializedName("tagged_user_data") val tagged_user_data: CommentResponse.TaggedUserData? = null
)
