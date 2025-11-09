package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class ReferralRewardResponse(
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
        @SerializedName("days_left")
        val daysLeft: Int? = null,
        @SerializedName("my_referral_code")
        val myReferralCode: String? = null,
        @SerializedName("referral_list")
        val referralList: List<Referral>? = null
    ) {
        data class Referral(
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("email")
            val email: String? = null,
            @SerializedName("first_name")
            val firstName: String? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("last_name")
            val lastName: String? = null,
            @SerializedName("referral_code")
            val referralCode: String? = null,
            @SerializedName("username")
            val username: String? = null,
            @SerializedName("display_name_preference")
            val display_name_preference: String? = null,
            @SerializedName("image")
            val image: String? = null
        )
    }
}