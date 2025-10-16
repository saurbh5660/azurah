package com.live.azurah.model

data class ShopCategoryResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val current_page: Int? = null,
        val `data`: List<Data>? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null
    ) {
        data class Data(
            val created_at: String? = null,
            val deleted_at: Any? = null,
            val id: Int? = null,
            val image: String? = null,
            val image_thumb: String? = null,
            val is_deleted: String? = null,
            val name: String? = null,
            val status: String? = null,
            val updated_at: String? = null
        )
    }
}