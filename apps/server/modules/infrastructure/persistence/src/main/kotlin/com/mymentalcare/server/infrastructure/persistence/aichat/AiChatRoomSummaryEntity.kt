package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.AiChatRoomSummary
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "ai_chat_room_summaries")
class AiChatRoomSummaryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "last_summarized_message_id")
    val lastSummarizedMessageId: Long?,

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    val summary: String,

    @Column(name = "emotional_state")
    val emotionalState: String?,

    @Column(name = "active_topics", columnDefinition = "TEXT")
    val activeTopics: String?,

    @Column(name = "unresolved_questions", columnDefinition = "TEXT")
    val unresolvedQuestions: String?,

    @Column(name = "user_preferences", columnDefinition = "TEXT")
    val userPreferences: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): AiChatRoomSummary {
        return AiChatRoomSummary(
            id = id,
            roomId = roomId,
            memberId = memberId,
            lastSummarizedMessageId = lastSummarizedMessageId,
            summary = summary,
            emotionalState = emotionalState,
            activeTopics = activeTopics,
            unresolvedQuestions = unresolvedQuestions,
            userPreferences = userPreferences,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

fun AiChatRoomSummary.toEntity(): AiChatRoomSummaryEntity {
    val now = LocalDateTime.now()
    return AiChatRoomSummaryEntity(
        id = id,
        roomId = roomId,
        memberId = memberId,
        lastSummarizedMessageId = lastSummarizedMessageId,
        summary = summary,
        emotionalState = emotionalState,
        activeTopics = activeTopics,
        unresolvedQuestions = unresolvedQuestions,
        userPreferences = userPreferences,
        createdAt = createdAt ?: now,
        updatedAt = now,
    )
}
