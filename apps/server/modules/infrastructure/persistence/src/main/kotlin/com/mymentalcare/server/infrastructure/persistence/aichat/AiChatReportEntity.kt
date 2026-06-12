package com.mymentalcare.server.infrastructure.persistence.aichat

import com.mymentalcare.server.domain.aichat.AiChatReport
import com.mymentalcare.server.domain.aichat.AiChatReportSong
import com.mymentalcare.server.domain.aichat.AiChatReportType
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
@Table(name = "ai_chat_reports")
class AiChatReportEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "room_id", nullable = false)
    val roomId: Long,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "conversation_date", nullable = false)
    val conversationDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    val reportType: AiChatReportType,

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    val summary: String,

    @Column(name = "primary_emotion", nullable = false, length = 80)
    val primaryEmotion: String,

    @Column(name = "emotion_intensity")
    val emotionIntensity: Int?,

    @Column(name = "main_cause", nullable = false, length = 120)
    val mainCause: String,

    @Column(name = "emotional_flow", nullable = false, columnDefinition = "TEXT")
    val emotionalFlow: String,

    @Column(name = "today_sentence", nullable = false, length = 300)
    val todaySentence: String,

    @Column(name = "client_request_id", length = 80)
    val clientRequestId: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toDomain(songs: List<AiChatReportSong>): AiChatReport {
        return AiChatReport(
            id = id,
            roomId = roomId,
            memberId = memberId,
            conversationDate = conversationDate,
            reportType = reportType,
            summary = summary,
            primaryEmotion = primaryEmotion,
            emotionIntensity = emotionIntensity,
            mainCause = mainCause,
            emotionalFlow = emotionalFlow,
            todaySentence = todaySentence,
            clientRequestId = clientRequestId,
            songs = songs,
            createdAt = createdAt,
        )
    }
}

@Entity
@Table(name = "ai_chat_report_songs")
class AiChatReportSongEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "report_id", nullable = false)
    val reportId: Long,

    @Column(name = "song_order", nullable = false)
    val songOrder: Int,

    @Column(name = "title", nullable = false, length = 120)
    val title: String,

    @Column(name = "artist", nullable = false, length = 120)
    val artist: String,

    @Column(name = "reason", nullable = false, length = 300)
    val reason: String,

    @Column(name = "youtube_url", nullable = false, length = 500)
    val youtubeUrl: String,
) {
    fun toDomain(): AiChatReportSong {
        return AiChatReportSong(
            id = id,
            reportId = reportId,
            songOrder = songOrder,
            title = title,
            artist = artist,
            reason = reason,
            youtubeUrl = youtubeUrl,
        )
    }
}

fun AiChatReport.toEntity(): AiChatReportEntity {
    return AiChatReportEntity(
        id = id,
        roomId = roomId,
        memberId = memberId,
        conversationDate = conversationDate,
        reportType = reportType,
        summary = summary,
        primaryEmotion = primaryEmotion,
        emotionIntensity = emotionIntensity,
        mainCause = mainCause,
        emotionalFlow = emotionalFlow,
        todaySentence = todaySentence,
        clientRequestId = clientRequestId,
        createdAt = createdAt ?: LocalDateTime.now(),
    )
}

fun AiChatReportSong.toEntity(reportId: Long): AiChatReportSongEntity {
    return AiChatReportSongEntity(
        id = id,
        reportId = reportId,
        songOrder = songOrder,
        title = title,
        artist = artist,
        reason = reason,
        youtubeUrl = youtubeUrl,
    )
}
