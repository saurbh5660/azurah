package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class BibleQuestViewModel(
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
        @SerializedName("advice")
        val advice: String? = null,
        @SerializedName("bible_quest_advices")
        val bibleQuestAdvices: List<BibleQuestAdvice>? = null,
        @SerializedName("bible_quest_category")
        val bibleQuestCategory: BibleQuestCategory? = null,
        @SerializedName("bible_quest_category_id")
        val bibleQuestCategoryId: Int? = null,
        @SerializedName("bible_quest_challenges")
        val bibleQuestChallenges: List<BibleQuestChallenge?>? = null,
        @SerializedName("bible_quest_images")
        val bibleQuestImages: List<BibleQuestImage?>? = null,
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
        @SerializedName("id")
        val id: Int? = null,
        @SerializedName("image")
        val image: String? = null,
        @SerializedName("image_thumb")
        val imageThumb: String? = null,
        @SerializedName("is_challenge_completed")
        val isChallengeCompleted: Int? = null,
        @SerializedName("is_challenge_started")
        val isChallengeStarted: Int? = null,
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
        val updatedAt: String? = null
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
            @SerializedName("is_wishlist")
            var isWishlist: Int? = null,
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
                @SerializedName("isWishlist")
                var isWishlist: Int? = null,
                @SerializedName("product_category")
                val productCategory: ProductCategory? = null,
                @SerializedName("product_category_id")
                val productCategoryId: Int? = null,
                @SerializedName("product_images")
                val productImages: List<ProductImage?>? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null,
                @SerializedName("website_url")
                val websiteUrl: String? = null
            ) {
                data class ProductCategory(
                    @SerializedName("id")
                    val id: Int? = null,
                    @SerializedName("name")
                    val name: String? = null
                )

                data class ProductImage(
                    @SerializedName("id")
                    val id: Int? = null,
                    @SerializedName("image")
                    val image: String? = null,
                    @SerializedName("image_thumb")
                    val imageThumb: String? = null,
                    @SerializedName("product_id")
                    val productId: Int? = null
                )
            }
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
            @SerializedName("is_completed")
            var isCompleted: Int? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("title")
            val title: String? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,
            var dayNo: Int? = null,
            var isSelected: Boolean? = null,
            var currentDay: Int = 0,
            var isPassed: Boolean? = null,
            @SerializedName("completed_date")
            var completedDate: String? = null,
            @SerializedName("completed_time")
            var completedTime: String? = null,)

        data class BibleQuestImage(
            @SerializedName("bible_quest_id")
            val bibleQuestId: Int? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("image")
            val image: String? = null,
            @SerializedName("image_thumb")
            val imageThumb: String? = null,
            @SerializedName("is_deleted")
            val isDeleted: String? = null,
            @SerializedName("status")
            val status: String? = null,
            @SerializedName("type")
            val type: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null,


        )
    }
}