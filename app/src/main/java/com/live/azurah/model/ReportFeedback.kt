package com.live.azurah.model

data class ReportFeedback(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val created_at: String? = null,
        val description: String? = null,
        val id: Int? = null,
        val is_deleted: String? = null,
        val status: String? = null,
        val title: String? = null,
        val updated_at: String? = null,
        val user_id: Int? = null
    )
}