package com.live.azurah.model

data class CommentCommonResponse(
val body: CommentResponse? = null,
val code: Int? = null,
val message: String? = null,
val success: Boolean? = null
){
    data class Body(
        val current_page: Int? = null,
        val data: CommentResponse? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null,
        val unreadMessageCount: Int? = null,
        val unreadNotificationCount: Int? = null
    )
}
