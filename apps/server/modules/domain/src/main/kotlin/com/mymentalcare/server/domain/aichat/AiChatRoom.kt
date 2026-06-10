package com.mymentalcare.server.domain.aichat

import java.time.LocalDate

data class AiChatRoom(
    val id: Long,
    val memberId: Long,
    val chatbotCode: String,
    val conversationDate: LocalDate,
    val status: AiChatRoomStatus,
)
