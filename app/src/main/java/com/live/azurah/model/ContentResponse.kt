package com.live.azurah.model

data class ContentResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
) {
    data class Body(
        val description: String? = null,
        val id: Int? = null,
        val slug: String? = null,
        val title: String? = null,
    )
}