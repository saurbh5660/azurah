package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    val created: Int? = null,
    val created_at: String? = null,
    val deleted: Int? = null,
    val deleted_at: Any? = null,
    var description: String? = null,
    val id: Int? = null,
    val is_deleted: String? = null,
    var is_like: Int? = null,
    var like_count: Int? = null,
    var notification_id: Int? = null,
    val parent_transaction_id: Int? = null,
    val post_id: Int? = null,
    val status: String? = null,
    val updated: Int? = null,
    val updated_at: String? = null,
    val user: User? = null,
    val replies : ArrayList<Replies> = ArrayList(),
    @SerializedName(value = "post_comment_tags", alternate = ["prayer_comment_tags", "testimony_comment_tags", "community_forum_comment_tags"])
    val post_comment_tags : ArrayList<PostCommentTag> = ArrayList(),
    val tagged_user_data: TaggedUserData? = null,
    val user_id: Int? = null
) {
    data class User(
        val email: String? = null,
        val first_name: String? = null,
        val username: String? = null,
        val id: Int? = null,
        val image: String? = null,
        val image_thumb: String? = null,
        val last_name: String? = null
    )

    data class TaggedUserData(
        val email: String? = null,
        val first_name: String? = null,
        val username: String? = null,
        val id: Int? = null,
        val image: String? = null,
        val image_thumb: String? = null,
        val last_name: String? = null
    )
    data class PostCommentTag(
        val id: Int? = null,
        val user_id: Int? = null,
        val post_id: Int? = null,
        val user: User? = null,
        val post_comment_id: Int? = null,
    )
    data class Replies(
        val email: String? = null,
        val first_name: String? = null,
        val username: String? = null,
        val id: Int? = null,
        val parent_transaction_id: Int? = null,
        val tagged_user_id: Int? = null,
        val user_id: Int? = null,
        val post_id: Int? = null,
        var notification_id: Int? = null,
        var description: String? = null,
        var is_like: Int? = null,
        var like_count: Int? = null,
        val image: String? = null,
        val created_at: String? = null,
        val user: User? = null,
        val post_comment_tags : ArrayList<PostCommentTag> = ArrayList(),
        val tagged_user_data: TaggedUserData? = null)
}
