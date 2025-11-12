package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class BlockResposne(
    @SerializedName("body")
    val body: Body? = null,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("success")
    val success: Boolean? = null
) {
    data class Body(
        @SerializedName("current_page")
        val currentPage: Int? = null,
        @SerializedName("data")
        val data: ArrayList<Data>? = null,
        @SerializedName("per_page")
        val perPage: Int? = null,
        @SerializedName("total_count")
        val totalCount: Int? = null,
        @SerializedName("total_pages")
        val totalPages: Int? = null
    ) {
        data class Data(
            @SerializedName("block_by")
            val blockBy: Int? = null,
            @SerializedName("block_to")
            val blockTo: Int? = null,
            @SerializedName("block_to_user")
            val blockToUser: BlockToUser? = null,
            @SerializedName("created")
            val created: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted")
            val deleted: Int? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null
        ) {
            data class BlockToUser(
                @SerializedName("email")
                val email: String? = null,
                @SerializedName("first_name")
                val firstName: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("image_thumb")
                val imageThumb: String? = null,
                @SerializedName("last_name")
                val lastName: String? = null,
                val username: String? = null,
                var isFollowByMe: Int? = null,
                val isFollowByOther: Int? = null,
                val profile_type: Int? = null,
                val display_name_preference: Int? = null,

                )
        }
    }
}