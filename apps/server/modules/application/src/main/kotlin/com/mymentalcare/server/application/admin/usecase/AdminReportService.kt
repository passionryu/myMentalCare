package com.mymentalcare.server.application.admin.usecase

import com.mymentalcare.server.application.admin.AdminChatRoomNotFoundException
import com.mymentalcare.server.application.admin.AdminReportNotFoundException
import com.mymentalcare.server.application.admin.AdminSensitiveAccessReasonRequiredException
import com.mymentalcare.server.application.admin.port.AdminChatMessageRecord
import com.mymentalcare.server.application.admin.port.AdminReportPage
import com.mymentalcare.server.application.admin.port.AdminReportQueryInputPort
import com.mymentalcare.server.application.admin.port.AdminReportRecord
import com.mymentalcare.server.application.admin.port.AdminReportRepository
import com.mymentalcare.server.application.admin.recorder.AdminAuditLogRecorder
import com.mymentalcare.server.application.admin.request.AdminAuditLogRecordRequest
import com.mymentalcare.server.application.admin.request.AdminChatMessageSearchRequest
import com.mymentalcare.server.application.admin.request.AdminReportSearchRequest
import com.mymentalcare.server.application.admin.response.AdminChatMessageResponse
import com.mymentalcare.server.application.admin.response.AdminReportDetailResponse
import com.mymentalcare.server.application.admin.response.AdminReportEmotionPointResponse
import com.mymentalcare.server.application.admin.response.AdminReportPageResponse
import com.mymentalcare.server.application.admin.response.AdminReportSongResponse
import com.mymentalcare.server.application.admin.response.AdminReportSummaryResponse
import com.mymentalcare.server.application.member.MemberNotFoundException
import com.mymentalcare.server.application.member.port.MemberRepository
import com.mymentalcare.server.domain.admin.AdminAuditLogAction
import com.mymentalcare.server.domain.admin.AdminAuditLogTargetType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminReportService(
    private val adminReportRepository: AdminReportRepository,
    private val memberRepository: MemberRepository,
    private val adminAuditLogRecorder: AdminAuditLogRecorder,
) : AdminReportQueryInputPort {
    @Transactional(readOnly = true)
    override fun readReports(request: AdminReportSearchRequest): AdminReportPageResponse {
        val normalizedRequest = request.normalized()

        return adminReportRepository.findReports(
            memberId = normalizedRequest.memberId,
            date = normalizedRequest.date,
            keyword = normalizedRequest.keyword,
            page = normalizedRequest.page,
            size = normalizedRequest.size,
        ).toPageResponse()
    }

    @Transactional
    override fun readReport(adminMemberId: Long, reportId: Long): AdminReportDetailResponse {
        val adminMember = memberRepository.findById(adminMemberId) ?: throw MemberNotFoundException()
        val report = adminReportRepository.findReportById(reportId) ?: throw AdminReportNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.AI_CHAT_REPORT_VIEW,
                targetType = AdminAuditLogTargetType.AI_CHAT_REPORT,
                targetId = report.id,
                reason = "관리자 마음 리포트 상세 조회",
            ),
        )

        return report.toDetailResponse()
    }

    @Transactional
    override fun readChatMessages(request: AdminChatMessageSearchRequest): List<AdminChatMessageResponse> {
        val reason = request.reason?.trim()?.takeIf { it.isNotBlank() }
            ?: throw AdminSensitiveAccessReasonRequiredException()
        val adminMember = memberRepository.findById(request.adminMemberId) ?: throw MemberNotFoundException()
        val room = adminReportRepository.findChatRoom(request.roomId) ?: throw AdminChatRoomNotFoundException()

        adminAuditLogRecorder.record(
            AdminAuditLogRecordRequest(
                adminMemberId = adminMember.id,
                adminLoginId = adminMember.loginId,
                action = AdminAuditLogAction.AI_CHAT_HISTORY_VIEW,
                targetType = AdminAuditLogTargetType.AI_CHAT_ROOM,
                targetId = room.id,
                reason = reason,
            ),
        )

        return adminReportRepository.findMessagesByRoomId(room.id).map { it.toResponse() }
    }
}

private fun AdminReportPage.toPageResponse(): AdminReportPageResponse {
    return AdminReportPageResponse(
        reports = reports.map { it.toSummaryResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun AdminReportRecord.toSummaryResponse(): AdminReportSummaryResponse {
    return AdminReportSummaryResponse(
        id = id,
        roomId = roomId,
        memberId = memberId,
        conversationDate = conversationDate,
        primaryEmotion = primaryEmotion,
        emotionScore = emotionScore,
        mainCause = mainCause,
        createdAt = createdAt,
    )
}

private fun AdminReportRecord.toDetailResponse(): AdminReportDetailResponse {
    return AdminReportDetailResponse(
        id = id,
        roomId = roomId,
        memberId = memberId,
        conversationDate = conversationDate,
        reportType = reportType,
        summary = summary,
        primaryEmotion = primaryEmotion,
        emotionIntensity = emotionIntensity,
        emotionScore = emotionScore,
        mainCause = mainCause,
        emotionalFlow = emotionalFlow,
        todaySentence = todaySentence,
        songs = songs.map {
            AdminReportSongResponse(
                title = it.title,
                artist = it.artist,
                reason = it.reason,
                youtubeUrl = it.youtubeUrl,
            )
        },
        emotionTimeline = emotionTimeline.map {
            AdminReportEmotionPointResponse(label = it.label, score = it.score, reason = it.reason)
        },
        createdAt = createdAt,
    )
}

private fun AdminChatMessageRecord.toResponse(): AdminChatMessageResponse {
    return AdminChatMessageResponse(
        id = id,
        roomId = roomId,
        senderType = senderType.name,
        content = content,
        messageOrder = messageOrder,
        createdAt = createdAt,
    )
}
