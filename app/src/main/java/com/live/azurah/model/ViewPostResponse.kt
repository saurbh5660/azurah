package com.live.azurah.model

data class ViewPostResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val comment_count: Int? = null,
        val created: Int? = null,
        val created_at: String? = null,
        val deleted_at: Any? = null,
        val description: String? = null,
        val id: Int? = null,
        val is_bookmark: Int? = null,
        val is_deleted: String? = null,
        val is_like: Int? = null,
        val is_post_reported: Int? = null,
        val latitude: String? = null,
        val like_count: Int? = null,
        val location: String? = null,
        val longitude: String? = null,
        val post_images: List<PostImage?>? = null,
        val post_tags: List<Any?>? = null,
        val privacy_type: Int? = null,
        val status: String? = null,
        val updated: Int? = null,
        val updated_at: String? = null,
        val user: User? = null,
        val user_id: Int? = null
    ) {
        data class PostImage(
            val id: Int? = null,
            val image: String? = null,
            val image_thumb: String? = null,
            val post_id: Int? = null,
            val type: Int? = null
        )

        data class User(
            val email: String? = null,
            val first_name: String? = null,
            val id: Int? = null,
            val image: String? = null,
            val image_thumb: String? = null,
            val last_name: String? = null,
            val username: String? = null,
        )
    }
}