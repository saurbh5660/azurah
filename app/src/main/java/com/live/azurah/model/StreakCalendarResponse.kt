package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class StreakCalendarResponse(
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
        @SerializedName("month")
        val month: String? = null,
        @SerializedName("total_days")
        val totalDays: Int? = 0,
        @SerializedName("completed_days")
        val completedDays: Int? = 0,
        @SerializedName("current_streak")
        val currentStreak: Int? = 0,
        @SerializedName("best_streak")
        val bestStreak: Int? = 0,
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
