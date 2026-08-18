package com.live.azurah.model

import com.google.gson.annotations.SerializedName

data class ActiveChallengeResponse(
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
        @SerializedName("quest")
        val quest: Quest? = null,
        @SerializedName("challenge")
        val challenge: Challenge? = null
    ) {
        data class Quest(
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("title")
            val title: String? = null,
            @SerializedName("description")
            val description: String? = null,
            @SerializedName("bible_verse")
            val bibleVerse: String? = null,
            @SerializedName("bible_version")
            val bibleVersion: String? = null,
            @SerializedName("quick_facts")
            val quickFacts: String? = null,
            @SerializedName("is_premium")
            val isPremium: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("total_challenge_day_count")
            val totalChallengeDayCount: Int? = null,
            @SerializedName("total_completed_day_count")
            val totalCompletedDayCount: Int? = null
        )

        data class Challenge(
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("bible_quest_id")
            val bibleQuestId: Int? = null,
            @SerializedName("day_no")
            val dayNo: Int? = null,
            @SerializedName("title")
            val title: String? = null,
            @SerializedName("description")
            val description: String? = null,
            @SerializedName("read_time")
            val readTime: Int? = null,
            @SerializedName("key_verse")
            val keyVerse: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("created")
            val created: Int? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("deleted")
            val deleted: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: String? = null,
            @SerializedName("is_completed")
            val isCompleted: Int? = null,
            @SerializedName("question_count")
            val questionCount: Int? = null,
            @SerializedName("is_quiz_completed")
            val isQuizCompleted: Int? = null,
            @SerializedName("is_quiz_quit")
            val isQuizQuit: Int? = null,
            @SerializedName("is_devotional_completed")
            val isDevotionalCompleted: Int? = null,
            @SerializedName("is_prayer_completed")
            val isPrayerCompleted: Int? = null,
            @SerializedName("bible_quest_challenge_devotionals")
            val devotionals: ArrayList<Devotional>? = null,
            @SerializedName("bible_quest_challenge_prayers")
            val prayers: ArrayList<Prayer>? = null,
            @SerializedName("bible_quest_challenge_discussions")
            val discussions: ArrayList<Discussion>? = null
        ) {
            data class Devotional(
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("bible_quest_id")
                val bibleQuestId: Int? = null,
                @SerializedName("bible_quest_challenge_id")
                val bibleQuestChallengeId: Int? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("deleted")
                val deleted: Int? = null,
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null,
                @SerializedName("deleted_at")
                val deletedAt: String? = null
            )

            data class Prayer(
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("bible_quest_id")
                val bibleQuestId: Int? = null,
                @SerializedName("bible_quest_challenge_id")
                val bibleQuestChallengeId: Int? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("deleted")
                val deleted: Int? = null,
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null,
                @SerializedName("deleted_at")
                val deletedAt: String? = null
            )

            data class Discussion(
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("bible_quest_id")
                val bibleQuestId: Int? = null,
                @SerializedName("bible_quest_challenge_id")
                val bibleQuestChallengeId: Int? = null,
                @SerializedName("added_by")
                val addedBy: Int? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("deleted")
                val deleted: Int? = null,
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null,
                @SerializedName("deleted_at")
                val deletedAt: String? = null,
                @SerializedName("comment_count")
                val commentCount: Int? = null
            )
        }
    }
}
