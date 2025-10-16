package com.live.azurah.model

data class InterestResponse(
    val body: List<Body>? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val created_at: String? = null,
        val deleted_at: Any? = null,
        val id: Int? = null,
        var isSelected: Boolean = false,
        val image: String? = null,
        val image_thumb: String? = null,
        val is_deleted: String? = null,
        val name: String? = null,
        val status: String? = null,
        val updated_at: String? = null
    )
}