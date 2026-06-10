package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface JpaAiChatRoomRepository : JpaRepository<AiChatRoomEntity, Long> {
    fun findByMemberIdAndChatbotCodeAndConversationDate(
        memberId: Long,
        chatbotCode: String,
        conversationDate: LocalDate,
    ): AiChatRoomEntity?
}
