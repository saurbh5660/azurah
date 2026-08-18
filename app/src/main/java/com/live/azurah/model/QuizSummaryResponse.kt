package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class QuizSummaryResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("body") val body: QuizSummaryBody? = null
)

data class QuizSummaryBody(
    @SerializedName("title") val title: String? = null,
    @SerializedName("quest_title") val questTitle: String? = null,
    @SerializedName("day_no") val dayNo: Int? = null,
    @SerializedName("correct_count") val correctCount: Int? = null,
    @SerializedName("total_questions") val totalQuestions: Int? = null,
    @SerializedName("quiz_score") val quizScore: Int? = null,
    @SerializedName("lb_bonus") val lbBonus: Int? = null,
    @SerializedName("bible_quest_completed_points") val bibleQuestCompletedPoints: Int? = null,
    @SerializedName("quiz_score_points") val quizScorePoints: Int? = null,
    @SerializedName("consistency_bonus_points") val consistencyBonusPoints: Int? = null,
    @SerializedName("total_earned_today") val totalEarnedToday: Int? = null,
    @SerializedName("max_daily_points") val maxDailyPoints: Int? = null
)
