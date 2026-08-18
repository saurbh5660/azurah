package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class QuizSubmitRequest(
    @SerializedName("bible_quest_id") val bibleQuestId: Int,
    @SerializedName("bible_quest_challenge_id") val bibleQuestChallengeId: Int,
    @SerializedName("day_no") val dayNo: Int,
    @SerializedName("answers") val answers: List<QuizAnswerRequest>
)

data class QuizAnswerRequest(
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("selected_option") val selectedOption: String
)
