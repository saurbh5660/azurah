package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class ProductDetailResponse(
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
        @SerializedName("is_wishlist")
        val isWishlist: Int? = null,
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("price")
        val price: String? = null,
        @SerializedName("product_category")
        val productCategory: ProductCategory? = null,
        @SerializedName("product_category_id")
        val productCategoryId: Int? = null,
        @SerializedName("product_images")
        val productImages: List<ProductImage>? = null,
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
            val productId: Int? = null,
            var isSelected:Boolean = false
        )
    }
}