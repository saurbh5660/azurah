package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class BibleQuestListResponse(
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
        @SerializedName("current_page")
        val currentPage: Int? = null,
        @SerializedName("data")
        val data: ArrayList<Data>? = null,
        @SerializedName("per_page")
        val perPage: Int? = null,
        @SerializedName("total_count")
        val totalCount: Int? = null,
        @SerializedName("total_pages")
        val totalPages: Int? = null,
        val total_completed_challenges: Int? = null,
        val total_joined_challenges: Int? = null,
        val total_challenges: Int? = null,
    ) {
        data class Data(
            @SerializedName("advice")
            val advice: String? = null,
            @SerializedName("bible_quest_advices")
            val bibleQuestAdvices: ArrayList<BibleQuestAdvice>? = null,
            @SerializedName("bible_quest_category")
            val bibleQuestCategory: BibleQuestCategory? = null,
            @SerializedName("bible_quest_category_id")
            val bibleQuestCategoryId: Int? = null,
            @SerializedName("bible_quest_challenges")
            val bibleQuestChallenges: ArrayList<BibleQuestChallenge>? = null,
            @SerializedName("bible_verse")
            val bibleVerse: String? = null,
            @SerializedName("bible_version")
            val bibleVersion: String? = null,
            @SerializedName("created")
            val created: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("description")
            val description: String? = null,
            @SerializedName("short_description")
            val shortDescription: String? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("image")
            val image: String? = null,
            @SerializedName("bible_quest_images")
            val bibleQuestImages: List<BibleQuestViewModel.Body.BibleQuestImage>? = null,
            @SerializedName("image_thumb")
            val imageThumb: String? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("is_premium")
            val isPremium: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("title")
            val title: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            @SerializedName("is_challenge_completed")
            val isChallengeCompleted: Int? = null,
            @SerializedName("is_challenge_started")
            val isChallengeStarted: Int? = null,
        ) {
            data class BibleQuestAdvice(
                @SerializedName("bible_quest_id")
                val bibleQuestId: Int? = null,
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("deleted")
                val deleted: Int? = null,
                @SerializedName("deleted_at")
                val deletedAt: Any? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("product")
                val product: Product? = null,
                @SerializedName("product_id")
                val productId: Int? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null
            ) {
                data class Product(
                    @SerializedName("created_at")
                    val createdAt: String? = null,
                    @SerializedName("deleted_at")
                    val deletedAt: Any? = null,
                    @SerializedName("description")
                    val description: String? = null,
                    @SerializedName("id")
                    val id: Int? = null,
                    @SerializedName("is_deleted")
                    val isDeleted: String? = null,
                    @SerializedName("name")
                    val name: String? = null,
                    @SerializedName("price")
                    val price: String? = null,
                    @SerializedName("product_category_id")
                    val productCategoryId: Int? = null,
                    @SerializedName("status")
                    val status: String? = null,
                    @SerializedName("updated_at")
                    val updatedAt: String? = null,
                    @SerializedName("website_url")
                    val websiteUrl: String? = null
                )
            }

            data class BibleQuestCategory(
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("deleted_at")
                val deletedAt: Any? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: Any? = null,
                @SerializedName("image_thumb")
                val imageThumb: Any? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("name")
                val name: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null
            )

            data class BibleQuestChallenge(
                @SerializedName("bible_quest_id")
                val bibleQuestId: Int? = null,
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("created_at")
                val createdAt: String? = null,
                @SerializedName("deleted")
                val deleted: Int? = null,
                @SerializedName("deleted_at")
                val deletedAt: Any? = null,
                @SerializedName("description")
                val description: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("is_deleted")
                val isDeleted: String? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("title")
                val title: String? = null,
                @SerializedName("updated")
                val updated: Int? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null
            )
        }
    }
}