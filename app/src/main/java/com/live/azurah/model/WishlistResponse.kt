package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class WishlistResponse(
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
        val `data`: List<Data>? = null,
        @SerializedName("per_page")
        val perPage: Int? = null,
        @SerializedName("total_count")
        val totalCount: Int? = null,
        @SerializedName("total_pages")
        val totalPages: Int? = null
    ) {
        data class Data(
            @SerializedName("created")
            val created: Int? = null,
            var isSelected: Boolean = false,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted")
            val deleted: Int? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
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
            val updatedAt: String? = null,
            @SerializedName("user")
            val user: User? = null,
            @SerializedName("user_id")
            val userId: Int? = null
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
                @SerializedName("product_images")
                val productImages: List<ProductImage?>? = null,
                @SerializedName("status")
                val status: String? = null,
                @SerializedName("updated_at")
                val updatedAt: String? = null,
                @SerializedName("website_url")
                val websiteUrl: String? = null
            ) {
                data class ProductImage(
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
                    @SerializedName("product_id")
                    val productId: Int? = null,
                    @SerializedName("status")
                    val status: String? = null,
                    @SerializedName("type")
                    val type: Int? = null,
                    @SerializedName("updated_at")
                    val updatedAt: String? = null
                )
            }

            data class User(
                @SerializedName("first_name")
                val firstName: String? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("image")
                val image: String? = null,
                @SerializedName("image_thumb")
                val imageThumb: String? = null,
                @SerializedName("last_name")
                val lastName: String? = null
            )
        }
    }
}