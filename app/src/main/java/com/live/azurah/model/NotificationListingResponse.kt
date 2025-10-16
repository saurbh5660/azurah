package com.live.azurah.model

data class NotificationListingResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val current_page: Int? = null,
        val data: ArrayList<Data>? = null,
        val per_page: Int? = null,
        val total_count: Int? = null,
        val total_pages: Int? = null
    ) {
        data class Data(
            val created_at: String? = null,
            val entity_id: Int? = null,
            val entity_type: String? = null,
            val id: Int? = null,
            var is_read: String? = null,
            val message: String? = null,
            val notification_receiver: NotificationReceiver? = null,
            val notification_sender: NotificationSender? = null,
            val receiver_id: Int? = null,
            val sender_id: Int? = null,
            val type: Int? = null,
            val updated_at: String? = null
        ) {
            data class NotificationReceiver(
                val email: String? = null,
                val first_name: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val last_name: String? = null
            )

            data class NotificationSender(
                val email: String? = null,
                val first_name: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val last_name: String? = null
            )
        }
    }
}