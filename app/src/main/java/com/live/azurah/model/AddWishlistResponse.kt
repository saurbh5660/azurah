package com.live.azurah.model

data class AddWishlistResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val created: Int? = null,
        val created_at: String? = null,
        val deleted: Int? = null,
        val deleted_at: Any? = null,
        val id: Int? = null,
        val is_deleted: String? = null,
        val product_id: Int? = null,
        val status: String? = null,
        val updated: Int? = null,
        val updated_at: String? = null,
        val user_id: Int? = null
    )
}