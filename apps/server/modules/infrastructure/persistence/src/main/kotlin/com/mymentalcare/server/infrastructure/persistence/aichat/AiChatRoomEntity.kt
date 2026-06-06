package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.AiChatRoom
import com.mymentalcare.server.domain.aichat.AiChatRoomStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "ai_chat_rooms")
class AiChatRoomEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "chatbot_code", nullable = false)
    val chatbotCode: String,

    @Column(name = "conversation_date", nullable = false)
    val conversationDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: AiChatRoomStatus,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): AiChatRoom {
        return AiChatRoom(
            id = id,
            memberId = memberId,
            chatbotCode = chatbotCode,
            conversationDate = conversationDate,
            status = status,
        )
    }
}

fun AiChatRoom.toEntity(): AiChatRoomEntity {
    return AiChatRoomEntity(
        id = id,
        memberId = memberId,
        chatbotCode = chatbotCode,
        conversationDate = conversationDate,
        status = status,
    )
}
