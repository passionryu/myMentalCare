package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
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
@Table(name = "ai_chat_check_ins")
class AiChatCheckInEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "segment_id", nullable = false)
    val segmentId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    val templateType: AiChatCheckInTemplateType,

    @Column(name = "answers_json", nullable = false, columnDefinition = "TEXT")
    val answersJson: String,

    @Column(name = "summary_text", nullable = false)
    val summaryText: String,

    @Column(name = "is_crisis_detected", nullable = false)
    val isCrisisDetected: Boolean,

    @Column(name = "detected_keywords")
    val detectedKeywords: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
