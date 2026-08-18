package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class QuizQuitRequest(
    @SerializedName("bible_quest_id") val bibleQuestId: Int,
    @SerializedName("bible_quest_challenge_id") val bibleQuestChallengeId: Int,
    @SerializedName("day_no") val dayNo: Int
)
