package com.live.azurah.model

data class CommunityCategoryResponse(
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
            val color_code: String? = null,
            val created_at: String? = null,
            val deleted_at: Any? = null,
            val id: Int? = null,
            val image: Any? = null,
            val image_thumb: Any? = null,
            val is_deleted: String? = null,
            val name: String? = null,
            val status: String? = null,
            val updated_at: String? = null
        )
    }
}