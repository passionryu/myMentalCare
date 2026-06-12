package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.AiChatSegment
import com.mymentalcare.server.domain.aichat.AiChatSegmentStartType
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
@Table(name = "ai_chat_segments")
class AiChatSegmentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Column(name = "segment_order", nullable = false)
    val segmentOrder: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "start_type", nullable = false)
    val startType: AiChatSegmentStartType,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "client_request_id")
    val clientRequestId: String?,

    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): AiChatSegment {
        return AiChatSegment(
            id = id,
            roomId = roomId,
            segmentOrder = segmentOrder,
            startType = startType,
            title = title,
            clientRequestId = clientRequestId,
            startedAt = startedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

fun AiChatSegment.toEntity(): AiChatSegmentEntity {
    return AiChatSegmentEntity(
        id = id,
        roomId = roomId,
        segmentOrder = segmentOrder,
        startType = startType,
        title = title,
        clientRequestId = clientRequestId,
        startedAt = startedAt ?: LocalDateTime.now(),
        createdAt = createdAt ?: LocalDateTime.now(),
        updatedAt = updatedAt ?: LocalDateTime.now(),
    )
}
