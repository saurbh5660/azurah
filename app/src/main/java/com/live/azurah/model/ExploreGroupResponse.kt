package com.live.azurah.model

data class ExploreGroupResponse(
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
            val added_by: Int? = null,
            val created: Int? = null,
            val created_at: String? = null,
            val deleted_at: Any? = null,
            val description: String? = null,
            val group_category: GroupCategory? = null,
            val group_category_id: Int? = null,
            val group_member_count: Int? = null,
            val id: Int? = null,
            val image: String? = null,
            val image_thumb: String? = null,
            val is_deleted: String? = null,
            val is_joined: Int? = null,
            val member_limit: Int? = null,
            val name: String? = null,
            val request_status: Int? = null,
            val status: String? = null,
            val updated: Int? = null,
            val updated_at: String? = null,
            val user: User? = null
        ) {
            data class GroupCategory(
                val created: Int? = null,
                val created_at: String? = null,
                val deleted: Int? = null,
                val deleted_at: Any? = null,
                val id: Int? = null,
                val image: Any? = null,
                val image_thumb: Any? = null,
                val is_deleted: String? = null,
                val name: String? = null,
                val status: String? = null,
                val updated: Int? = null,
                val updated_at: String? = null
            )

            data class User(
                val country_code: String? = null,
                val email: String? = null,
                val first_name: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val last_name: String? = null,
                val phone: String? = null,
                val role: String? = null,
                val username: Any? = null
            )
        }
    }
}