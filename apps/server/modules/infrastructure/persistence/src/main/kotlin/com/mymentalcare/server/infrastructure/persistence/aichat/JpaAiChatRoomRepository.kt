package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface JpaAiChatRoomRepository : JpaRepository<AiChatRoomEntity, Long> {
    fun findByMemberIdAndChatbotCodeAndConversationDate(
        memberId: Long,
        chatbotCode: String,
        conversationDate: LocalDate,
    ): AiChatRoomEntity?

    fun findByMemberIdOrderByConversationDateDescIdDesc(memberId: Long): List<AiChatRoomEntity>

    fun findByIdAndMemberId(roomId: Long, memberId: Long): AiChatRoomEntity?
}
