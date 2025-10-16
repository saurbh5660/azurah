package com.live.azurah.model

data class DashboardDataResposne(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val allResult: List<AllResult?>? = null,
        val result: Result? = null
    ) {
        data class AllResult(
            val created: Int? = null,
            val created_at: String? = null,
            val deleted: Int? = null,
            val deleted_at: Any? = null,
            val description: String? = null,
            val id: Int? = null,
            val image: String? = null,
            val image_thumb: String? = null,
            val is_deleted: String? = null,
            val slug: String? = null,
            val status: String? = null,
            val title: String? = null,
            val updated: Int? = null,
            val updated_at: String? = null
        )

        data class Result(
            val created: Int? = null,
            val created_at: String? = null,
            val deleted: Int? = null,
            val deleted_at: Any? = null,
            val description: String? = null,
            val id: Int? = null,
            val image: Any? = null,
            val image_thumb: Any? = null,
            val is_deleted: String? = null,
            val slug: String? = null,
            val status: String? = null,
            val title: String? = null,
            val updated: Int? = null,
            val updated_at: String? = null
        )
    }
}