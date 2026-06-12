package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.CrisisDetectionEvent
import com.mymentalcare.server.domain.aichat.CrisisDetectionSourceType
import com.mymentalcare.server.domain.aichat.CrisisHandledAction
import com.mymentalcare.server.domain.aichat.CrisisRiskLevel
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
@Table(name = "crisis_detection_events")
class CrisisDetectionEventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    val sourceType: CrisisDetectionSourceType = CrisisDetectionSourceType.MESSAGE,

    @Column(name = "message_id")
    val messageId: Long?,

    @Column(name = "check_in_id")
    val checkInId: Long?,

    @Column(name = "detected_keywords", nullable = false)
    val detectedKeywords: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    val riskLevel: CrisisRiskLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "handled_action", nullable = false)
    val handledAction: CrisisHandledAction,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(): CrisisDetectionEvent {
        return CrisisDetectionEvent(
            id = id,
            memberId = memberId,
            roomId = roomId,
            sourceType = sourceType,
            messageId = messageId,
            checkInId = checkInId,
            detectedKeywords = detectedKeywords.split(",").filter { it.isNotBlank() },
            riskLevel = riskLevel,
            handledAction = handledAction,
            createdAt = createdAt,
        )
    }
}

fun CrisisDetectionEvent.toEntity(): CrisisDetectionEventEntity {
    return CrisisDetectionEventEntity(
        id = id,
        memberId = memberId,
        roomId = roomId,
        sourceType = sourceType,
        messageId = messageId,
        checkInId = checkInId,
        detectedKeywords = detectedKeywords.joinToString(","),
        riskLevel = riskLevel,
        handledAction = handledAction,
        createdAt = createdAt ?: LocalDateTime.now(),
    )
}
