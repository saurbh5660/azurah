package com.live.azurah.model


import com.google.gson.annotations.SerializedName

data class InboxResponse(
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
        val totalPages: Int? = null
    ) {
        data class Data(
            @SerializedName("created")
            val created: Long? = null,
            @SerializedName("created_at")
            val createdAt: String? = null,
            @SerializedName("deleted")
            val deleted: Int? = null,
            @SerializedName("deleted_at")
            val deletedAt: Any? = null,
            @SerializedName("deleted_id")
            val deletedId: Int? = null,
            @SerializedName("group")
            val group: Group? = null,
            @SerializedName("group_id")
            val groupId: Int? = null,
            @SerializedName("id")
            val id: Int? = null,
            @SerializedName("last_msg_id")
            val lastMsgId: Int? = null,
            @SerializedName("chat_messages")
            val message: ArrayList<Message>? = null,
            @SerializedName("receiver_id")
            val receiverId: Int? = null,
            @SerializedName("sender_id")
            val senderId: Int? = null,
            @SerializedName("unread_count")
            val unreadCount: Int? = null,
            @SerializedName("updated")
            val updated: Int? = null,
            @SerializedName("updated_at")
            val updatedAt: String? = null
        ) {

            data class Group(
                var id: Int? = null,
                var group_category_id: Int? = null,
                var name: String? = null,
                var image: String? = null,
                var image_thumb: String? = null,
                var description: String? = null,
            )
            data class Message(
                @SerializedName("constant_id")
                val constantId: Int? = null,
                @SerializedName("created")
                val created: Int? = null,
                @SerializedName("group_id")
                val groupId: Any? = null,
                @SerializedName("id")
                val id: Int? = null,
                @SerializedName("is_read")
                val isRead: String? = null,
                @SerializedName("message")
                val message: String? = null,
                @SerializedName("message_receiver")
                val messageReceiver: MessageReceiver? = null,
                @SerializedName("message_sender")
                val messageSender: MessageSender? = null,
                @SerializedName("msg_type")
                val msgType: Int? = null,
                @SerializedName("read_at")
                val readAt: Int? = null,
                @SerializedName("receiver_id")
                val receiverId: Int? = null,
                @SerializedName("sender_id")
                val senderId: Int? = null,
                @SerializedName("updated")
                val updated: Int? = null
            ) {
                data class MessageReceiver(
                    @SerializedName("country_code")
                    val countryCode: String? = null,
                    @SerializedName("email")
                    val email: String? = null,
                    @SerializedName("first_name")
                    val firstName: String? = null,
                    @SerializedName("id")
                    val id: Int? = null,
                    @SerializedName("image")
                    val image: String? = null,
                    @SerializedName("image_thumb")
                    val imageThumb: String? = null,
                    @SerializedName("last_name")
                    val lastName: String? = null,
                    @SerializedName("phone")
                    val phone: String? = null,
                    @SerializedName("username")
                    val username: String? = null
                )

                data class MessageSender(
                    @SerializedName("country_code")
                    val countryCode: String? = null,
                    @SerializedName("email")
                    val email: String? = null,
                    @SerializedName("first_name")
                    val firstName: String? = null,
                    @SerializedName("id")
                    val id: Int? = null,
                    @SerializedName("image")
                    val image: String? = null,
                    @SerializedName("image_thumb")
                    val imageThumb: String? = null,
                    @SerializedName("last_name")
                    val lastName: String? = null,
                    @SerializedName("phone")
                    val phone: String? = null,
                    @SerializedName("username")
                    val username: String? = null
                )
            }
        }
    }
}