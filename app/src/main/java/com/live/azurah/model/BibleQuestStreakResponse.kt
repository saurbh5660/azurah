package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class BibleQuestStreakResponse(
    @SerializedName("success")
    val success: Boolean? = null,
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("body")
    val body: Body? = null
) {
    data class Body(
        @SerializedName("current_streak")
        val currentStreak: Int? = 0,
        @SerializedName("best_streak")
        val bestStreak: Int? = 0,
        @SerializedName("last_completed_date")
        val lastCompletedDate: String? = null,
        @SerializedName("protection_available")
        val protectionAvailable: Int? = 0,
        @SerializedName("growth_level")
        val growthLevel: String? = null,
        @SerializedName("next_level")
        val nextLevel: String? = null,
        @SerializedName("days_to_next_level")
        val daysToNextLevel: Int? = 0,
        @SerializedName("current_week")
        val currentWeek: CurrentWeek? = null
    ) {
        data class CurrentWeek(
            @SerializedName("week_start")
            val weekStart: String? = null,
            @SerializedName("week_end")
            val weekEnd: String? = null,
            @SerializedName("days")
            val days: List<Day>? = null
        ) {
            data class Day(
                @SerializedName("date")
                val date: String? = null,
                @SerializedName("day")
                val day: String? = null,
                @SerializedName("status")
                val status: String? = null
            )
        }
    }
}
