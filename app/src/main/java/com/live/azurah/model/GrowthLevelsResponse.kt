package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class GrowthLevelsResponse(
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
        @SerializedName("levels")
        val levels: List<GrowthLevel>? = null,
        @SerializedName("current_streak")
        val currentStreak: Int? = null,
        @SerializedName("best_streak")
        val bestStreak: Int? = null,
        @SerializedName("days_to_next_level")
        val daysToNextLevel: Int? = null
    )

    data class GrowthLevel(
        @SerializedName("level")
        val level: String? = null,
        @SerializedName("min_streak")
        val minStreak: Int? = null,
        @SerializedName("unlocked")
        val unlocked: Boolean? = null,
        @SerializedName("current")
        val current: Boolean? = null
    )
}
