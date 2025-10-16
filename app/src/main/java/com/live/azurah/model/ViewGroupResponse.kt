package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class ViewGroupResponse(
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
        @SerializedName("added_by")
        val addedBy: Int? = null,
        @SerializedName("created")
        val created: Int? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("deleted_at")
        val deletedAt: Any? = null,
        @SerializedName("description")
        val description: String? = null,
        @SerializedName("group_category")
        val groupCategory: GroupCategory? = null,
        @SerializedName("group_category_id")
        val groupCategoryId: Int? = null,
        @SerializedName("group_member_count")
        val groupMemberCount: Int? = null,
        @SerializedName("group_members")
        val groupMembers: List<GroupMembers>? = null,
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("image")
        val image: String? = null,
        @SerializedName("image_thumb")
        val imageThumb: String? = null,
        @SerializedName("is_deleted")
        val isDeleted: String? = null,
        @SerializedName("is_joined")
        val isJoined: Int? = null,
        @SerializedName("member_limit")
        val memberLimit: Int? = null,
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("request_status")
        val requestStatus: Int? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("updated")
        val updated: Int? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("user")
        val user: User? = null
    ) {

        data class GroupMembers(
            val user: User? = null,
            var isFollowByMe: Int? = null,
            val isFollowByOther: Int? = null,
            val user_id: Int? = null,
            val group_id: Int? = null,
            val id: Int? = null,
        )

        data class GroupCategory(
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
            @SerializedName("image")
            val image: Any? = null,
            @SerializedName("image_thumb")
            val imageThumb: Any? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("name")
            val name: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null
        )

        data class User(
            @SerializedName("country_code")
            val countryCode: String? = null,
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
            @SerializedName("phone")
            val phone: String? = null,
            @SerializedName("role")
            val role: String? = null,
            @SerializedName("username")
            val username: String? = null,
            val profile_type: Int? = null,
        )
    }
}