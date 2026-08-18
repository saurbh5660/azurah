package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class QuizQuestionResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("body") val body: List<QuizQuestion>? = null
)

data class QuizQuestion(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("bible_quest_id") val bibleQuestId: Int? = null,
    @SerializedName("bible_quest_challenge_id") val bibleQuestChallengeId: Int? = null,
    @SerializedName("question") val question: String? = null,
    @SerializedName("option_a") val optionA: String? = null,
    @SerializedName("context_description_a") val contextDescriptionA: String? = null,
    @SerializedName("option_b") val optionB: String? = null,
    @SerializedName("context_description_b") val contextDescriptionB: String? = null,
    @SerializedName("option_c") val optionC: String? = null,
    @SerializedName("context_description_c") val contextDescriptionC: String? = null,
    @SerializedName("option_d") val optionD: String? = null,
    @SerializedName("context_description_d") val contextDescriptionD: String? = null,
    @SerializedName("correct_option") val correctOption: String? = null,
    @SerializedName("context_description_correct_option") val contextDescriptionCorrectOption: String? = null
)
