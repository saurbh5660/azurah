package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class ProfileResponse(
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
        @SerializedName("bio")
        val bio: String? = null,
        @SerializedName("christian_journey")
        val christianJourney: String? = null,
        @SerializedName("country")
        val country: String? = null,
        @SerializedName("country_code")
        val countryCode: String? = null,
        @SerializedName("cover_image")
        val coverImage: String? = null,
        @SerializedName("cover_image_thumb")
        val coverImageThumb: String? = null,
        @SerializedName("created_at")
        val createdAt: String? = null,
        @SerializedName("device_token")
        val deviceToken: String? = null,
        @SerializedName("device_type")
        val deviceType: Int? = null,
        @SerializedName("display_name_preference")
        val displayNamePreference: Int? = null,
        @SerializedName("dob")
        val dob: String? = null,
        @SerializedName("email")
        val email: String? = null,
        @SerializedName("first_name")
        val firstName: String? = null,
        @SerializedName("form_step")
        val formStep: Int? = null,
        @SerializedName("gender")
        val gender: String? = null,
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("image")
        val image: String? = null,
        @SerializedName("image_thumb")
        val imageThumb: String? = null,
        @SerializedName("is_forum_push")
        val isForumPush: String? = null,
        @SerializedName("is_new_bible_quest_push")
        val isNewBibleQuestPush: String? = null,
        @SerializedName("is_newsletter")
        val isNewsletter: String? = null,
        @SerializedName("is_notification")
        val isNotification: String? = null,
        @SerializedName("is_mention_push")
        val is_mention_push: String? = null,
        @SerializedName("is_post_comment_mention_push")
        val is_post_comment_mention_push: String? = null,
        @SerializedName("is_otp_verified")
        val isOtpVerified: Int? = null,
        @SerializedName("is_post_push")
        val isPostPush: String? = null,
        @SerializedName("is_prayer_push")
        val isPrayerPush: String? = null,
        @SerializedName("is_profile_completed")
        val isProfileCompleted: String? = null,
        @SerializedName("is_quest_complete_remind_push")
        val isQuestCompleteRemindPush: String? = null,
        @SerializedName("is_quest_push")
        val isQuestPush: String? = null,
        @SerializedName("is_shop_push")
        val isShopPush: String? = null,
        @SerializedName("is_testimony_push")
        val isTestimonyPush: String? = null,
        @SerializedName("last_name")
        val lastName: String? = null,
        @SerializedName("latitude")
        val latitude: String? = null,
        @SerializedName("location")
        val location: String? = null,
        @SerializedName("login_time")
        val loginTime: String? = null,
        @SerializedName("longitude")
        val longitude: String? = null,
        @SerializedName("otp")
        val otp: Int? = null,
        @SerializedName("password")
        val password: String? = null,
        @SerializedName("phone")
        val phone: String? = null,
        @SerializedName("post_type")
        val postType: Int? = null,
        @SerializedName("profile_type")
        val profileType: Int? = null,
        @SerializedName("reset_token")
        val resetToken: String? = null,
        @SerializedName("role")
        val role: String? = null,
        @SerializedName("social_id")
        val socialId: String? = null,
        @SerializedName("social_type")
        val socialType: Int? = null,
        @SerializedName("status")
        val status: String? = null,
        @SerializedName("updated_at")
        val updatedAt: String? = null,
        @SerializedName("user_answers")
        val userAnswers: List<UserAnswer?>? = null,
        @SerializedName("user_interests")
        val userInterests: List<UserInterest?>? = null,
        @SerializedName("user_timezone")
        val userTimezone: String? = null,
        @SerializedName("username")
        val username: String? = null,
        @SerializedName("followerCount")
        val followerCount: String? = null,
        @SerializedName("followingCount")
        val followingCount: String? = null,
        @SerializedName("postCount")
        val postCount: String? = null,
        var isFollowByMe: Int? = null,
        var isFollowByOther: Int? = null,
        var is_block_by_admin: String? = null,
    ) {
        data class UserAnswer(
            @SerializedName("created")
            val created: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("description")
            val description: String? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("question")
            val question: Question? = null,
            @SerializedName("question_id")
            val questionId: Int? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            @SerializedName("user_id")
            val userId: Int? = null
        ) {
            data class Question(
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("deleted_at")
                val deletedAt: Any? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null
            )
        }

        data class UserInterest(
            @SerializedName("created")
            val created: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("interest")
            val interest: Interest? = null,
            @SerializedName("interest_id")
            val interestId: Int? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            @SerializedName("user_id")
            val userId: Int? = null
        ) {
            data class Interest(
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("deleted_at")
                val deletedAt: Any? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("image_thumb")
                val imageThumb: String? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("name")
                val name: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null
            )
        }
    }
}