package com.live.azurah.model

data class SavedPostResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val current_page: Int? = null,
        val `data`: List<Data?>? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null
    ) {
        data class Data(
            val created: Int? = null,
            val created_at: String? = null,
            val deleted: Int? = null,
            val deleted_at: Any? = null,
            val id: Int? = null,
            val is_deleted: String? = null,
            val post: Post? = null,
            val post_id: Int? = null,
            val status: String? = null,
            val updated: Int? = null,
            val updated_at: String? = null,
            val user: User? = null,
            val user_id: Int? = null
        ) {
            data class Post(
                val created: Int? = null,
                val created_at: String? = null,
                val deleted_at: Any? = null,
                val description: String? = null,
                val id: Int? = null,
                val is_bookmark: Int? = null,
                val is_like: Int? = null,
                val like_count: Int? = null,
                val user: User? = null,
                val comment_count: Int? = null,
                val is_deleted: String? = null,
                val latitude: String? = null,
                val location: String? = null,
                val longitude: String? = null,
                val post_images: List<PostImage?>? = null,
                val privacy_type: Int? = null,
                val status: String? = null,
                val updated: Int? = null,
                val updated_at: String? = null,
                val user_id: Int? = null
            ) {
                data class PostImage(
                    val id: Int? = null,
                    val image: String? = null,
                    val image_thumb: String? = null,
                    val post_id: Int? = null,
                    val type: Int? = null
                )
            }

            data class User(
                val first_name: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val last_name: String? = null,
                val username: String? = null
            )
        }
    }
}