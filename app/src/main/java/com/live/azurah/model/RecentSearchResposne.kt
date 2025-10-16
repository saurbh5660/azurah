package com.live.azurah.model

data class RecentSearchResposne(
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
            val id: Int? = null,
            val is_deleted: String? = null,
            val search_string: String? = null,
            val type: Int? = null,
            val updated: Int? = null,
            val updated_at: String? = null,
            val user_id: Int? = null
        )
    }
}