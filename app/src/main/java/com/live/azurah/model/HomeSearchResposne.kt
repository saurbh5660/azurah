package com.live.azurah.model

data class HomeSearchResposne(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val posts: Posts? = null,
        val users: Users? = null
    ) {
        data class Posts(
            val current_page: Int? = null,
            val data: List<Data>? = null,
            val per_page: Int? = null,
            val total_count: Int? = null,
            val total_pages: Int? = null,
            val unreadMessageCount: Int? = null,
            val unreadNotificationCount: Int? = null
        ) {
            data class Data(
                val comment_count: Int? = null,
                val created: Int? = null,
                val created_at: String? = null,
                val deleted_at: Any? = null,
                val description: String? = null,
                val id: Int? = null,
                val is_bookmark: Int? = null,
                val is_deleted: String? = null,
                val is_like: Int? = null,
                val is_post_reported: Int? = null,
                val latitude: Any? = null,
                val like_count: Int? = null,
                val location: Any? = null,
                val longitude: Any? = null,
                val post_images: List<PostImage?>? = null,
                val post_tags: List<Any?>? = null,
                val privacy_type: Int? = null,
                val status: String? = null,
                val suggestedUsers: List<SuggestedUser?>? = null,
                val updated: Int? = null,
                val updated_at: String? = null,
                val user: User? = null,
                val user_id: Int? = null
            ) {
                data class PostImage(
                    val id: Int? = null,
                    val image: String? = null,
                    val image_thumb: String? = null,
                    val post_id: Int? = null,
                    val type: Int? = null
                )

                data class SuggestedUser(
                    val bio: String? = null,
                    val christian_journey: String? = null,
                    val country: String? = null,
                    val country_code: String? = null,
                    val cover_image: String? = null,
                    val cover_image_thumb: String? = null,
                    val created_at: String? = null,
                    val delete_reason: String? = null,
                    val deleted_at: Any? = null,
                    val device_token: String? = null,
                    val device_type: Int? = null,
                    val display_name_preference: Int? = null,
                    val dob: String? = null,
                    val email: String? = null,
                    val first_name: String? = null,
                    val followerCount: Int? = null,
                    val followingCount: Int? = null,
                    val form_step: Int? = null,
                    val gender: String? = null,
                    val id: Int? = null,
                    val image: String? = null,
                    val image_thumb: String? = null,
                    val isFollowByMe: Int? = null,
                    val isFollowByOther: Int? = null,
                    val is_deleted: String? = null,
                    val is_forum_push: String? = null,
                    val is_new_bible_quest_push: String? = null,
                    val is_newsletter: String? = null,
                    val is_notification: String? = null,
                    val is_otp_verified: Int? = null,
                    val is_post_push: String? = null,
                    val is_prayer_push: String? = null,
                    val is_profile_completed: String? = null,
                    val is_quest_complete_remind_push: String? = null,
                    val is_quest_push: String? = null,
                    val is_shop_push: String? = null,
                    val is_testimony_push: String? = null,
                    val last_name: String? = null,
                    val latitude: String? = null,
                    val location: String? = null,
                    val login_time: String? = null,
                    val longitude: String? = null,
                    val otp: Int? = null,
                    val password: String? = null,
                    val phone: String? = null,
                    val post_type: Int? = null,
                    val profile_type: Int? = null,
                    val reset_token: String? = null,
                    val role: String? = null,
                    val social_id: String? = null,
                    val social_type: Int? = null,
                    val status: String? = null,
                    val updated_at: String? = null,
                    val user_timezone: String? = null,
                    val username: String? = null
                )

                data class User(
                    val email: String? = null,
                    val first_name: String? = null,
                    val id: Int? = null,
                    val display_name_preference: Int? = null,
                    val image: String? = null,
                    val image_thumb: String? = null,
                    val last_name: String? = null,
                    val profile_type: Int? = null,
                    val username: String? = null
                )
            }
        }

        data class Users(
            val current_page: Int? = null,
            val data: List<Data>? = null,
            val per_page: Int? = null,
            val total_count: Int? = null,
            val total_pages: Int? = null
        ) {
            data class Data(
                val country_code: String? = null,
                val email: String? = null,
                val first_name: String? = null,
                val followerCount: Int? = null,
                val followingCount: Int? = null,
                val id: Int? = null,
                val display_name_preference: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val isFollowByMe: Int? = null,
                val isFollowByOther: Int? = null,
                val last_name: String? = null,
                val phone: String? = null,
                val profile_type: Int? = null,
                val username: String? = null
            )
        }
    }
}