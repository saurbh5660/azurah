package com.live.azurah.model

data class PostLikesResposne(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val current_page: Int? = null,
        val data: ArrayList<Data>? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null
    ) {
        data class Data(
            val id: Int? = null,
            val post_id: Int? = null,
            val status: String? = null,
            val user: User? = null,
            val user_id: Int? = null,
            val username: String? = null,
            val first_name: String? = null,
            val last_name: String? = null,
            val image: String? = null,
            var isFollowByMe: Int? = null,
            var isFollowByOther: Int? = null,
            val profile_type: Int? = null,

        ) {
            data class User(
                val email: String? = null,
                val first_name: String? = null,
                val display_name_preference: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val last_name: String? = null,
                val username: String? = null
            )
        }
    }
}