package com.live.azurah.model

import java.util.ArrayList

data class CommunityForumResponse(
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
            val category: Category? = null,
            val prayer_category: Category? = null,
            val testimony_category: Category? = null,
            val category_id: Int? = null,
            val comment_count: Int? = null,
            var is_praise: Int? = null,
            var praise_count: Int? = null,
            val created_at: String? = null,
            val deleted_at: Any? = null,
            val description: String? = null,
            val id: Int? = null,
            val is_deleted: String? = null,
            var is_like: Int? = null,
            val is_reported: Int? = null,
            var like_count: Int? = null,
            val status: String? = null,
            val title: String? = null,
            val type: Int? = null,
            val updated_at: String? = null,
            val user: User? = null,
            val user_id: Int? = null
        ) {
            data class Category(
                val id: Int? = null,
                val name: String? = null
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
                val username: String? = null
            )
        }
    }
}