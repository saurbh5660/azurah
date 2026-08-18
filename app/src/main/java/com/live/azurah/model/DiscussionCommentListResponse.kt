package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class DiscussionCommentListResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("body") val body: Body? = null
) {
    data class Body(
        @SerializedName("total_pages") val total_pages: Int? = null,
        @SerializedName("total_count") val total_count: Int? = null,
        @SerializedName("current_page") val current_page: Int? = null,
        @SerializedName("per_page") val per_page: Int? = null,
        @SerializedName("data") val data: List<DiscussionCommentItem>? = null
    )
}
