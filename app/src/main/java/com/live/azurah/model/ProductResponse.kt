package com.live.azurah.model

data class ProductResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val current_page: Int? = null,
        val `data`: List<Data>? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null
    ) {
        data class Data(
            val created_at: String? = null,
            val deleted_at: Any? = null,
            val description: String? = null,
            val id: Int? = null,
            val is_best_seller: Int? = null,
            val is_popular: Int? = null,
            val is_deleted: String? = null,
            var is_wishlist: Int? = null,
            val name: String? = null,
            val price: String? = null,
            val product_category: ProductCategory? = null,
            val product_category_id: Int? = null,
            val product_images: List<ProductImage?>? = null,
            val status: String? = null,
            val updated_at: String? = null,
            val website_url: String? = null
        ) {
            data class ProductCategory(
                val id: Int? = null,
                val name: String? = null
            )

            data class ProductImage(
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val product_id: Int? = null
            )
        }
    }
}