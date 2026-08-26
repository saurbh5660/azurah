package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class LeaderboardResponse(
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("body")
    val body: Body? = null
) {
    data class Body(
        @SerializedName("view")
        val view: String? = null,
        @SerializedName("growth_level")
        val growthLevel: String? = null,
        @SerializedName("top3")
        val top3: List<LeaderboardItem>? = null,
        @SerializedName("rankings")
        val rankings: List<LeaderboardItem>? = null,
        @SerializedName("my_rank")
        val myRank: Int? = null,
        @SerializedName("my_points")
        val myPoints: Int? = null,
        @SerializedName("total_in_pool")
        val totalInPool: Int? = null
    )
}

data class LeaderboardGlobalResponse(
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("body")
    val body: Body? = null
) {
    data class Body(
        @SerializedName("week_start_date")
        val weekStartDate: String? = null,
        @SerializedName("top100")
        val top100: List<LeaderboardItem>? = null
    )
}

data class LeaderboardItem(
    @SerializedName("rank")
    val rank: Int? = null,
    @SerializedName("user_id")
    val userId: Int? = null,
    @SerializedName("total_points")
    val totalPoints: Int? = null,
    @SerializedName("growth_level")
    val growthLevel: String? = null,
    @SerializedName("user")
    val user: User? = null
) {
    data class User(
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("first_name")
        val firstName: String? = null,
        @SerializedName("last_name")
        val lastName: String? = null,
        @SerializedName("username")
        val username: String? = null,
        @SerializedName("profile_image")
        val profileImage: String? = null,
        @SerializedName("image")
        val image: String? = null
    )
}
