package com.live.azurah.model

data class MuteResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val group_id: String? = null,
        val isMutedByMe: String? = null,
        val isFollowByMe: Int? = null,
        val isFollowByOther: Int? = null,
        val messageRequest: Int? = null,
        val messageRequestSenderId: Int? = null,
        val isMutedByOther: String? = null,
        val isSenderBlockByAdmin: String? = null,
        val isReceiverBlockByAdmin: String? = null,
        val receiver_id: String? = null,
        val sender_id: String? = null,
        val isBlockedByMe: String? = null,
        val isBlockedByOther: String? = null,
        val senderProfileType: Int? = null,
        val receiverProfileType: Int? = null,
    )
}