package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.ChatMessage
import com.mymentalcare.server.domain.aichat.ChatMessageSenderType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Column(name = "segment_id")
    val segmentId: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    val senderType: ChatMessageSenderType,

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "message_order", nullable = false)
    val messageOrder: Int,

    @Column(name = "is_crisis_detected", nullable = false)
    val isCrisisDetected: Boolean,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            roomId = roomId,
            segmentId = segmentId,
            senderType = senderType,
            content = content,
            messageOrder = messageOrder,
            isCrisisDetected = isCrisisDetected,
            createdAt = createdAt,
        )
    }
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        roomId = roomId,
        segmentId = segmentId,
        senderType = senderType,
        content = content,
        messageOrder = messageOrder,
        isCrisisDetected = isCrisisDetected,
        createdAt = createdAt ?: LocalDateTime.now(),
    )
}
