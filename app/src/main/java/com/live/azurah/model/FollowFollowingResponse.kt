package com.live.azurah.model

data class FollowFollowingResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val current_page: Int? = null,
        val data: List<Data>? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null
    ) {
        data class Data(
            val created: Int? = null,
            val created_at: String? = null,
            val deleted: Int? = null,
            val deleted_at: Any? = null,
            val follow_by: Int? = null,
            val follow_by_user: FollowByUser? = null,
            val follow_to: Int? = null,
            val follow_to_user: FollowToUser? = null,
            val id: Int? = null,
            val is_deleted: String? = null,
            var status: Int? = null,
            var isFollowByMe: Int? = null,
            var profile_type: Int? = null,
            var isFollowByOther: Int? = null,
            val updated: Int? = null,
            val updated_at: String? = null
        ) {
            data class FollowByUser(
                val email: String? = null,
                val first_name: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val last_name: String? = null,
                val username: String? = null,
            )

            data class FollowToUser(
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
}