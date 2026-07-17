package com.mymentalcare.server.bootstrap.admin.web

import com.mymentalcare.server.application.admin.port.AdminReportQueryInputPort
import com.mymentalcare.server.application.admin.request.AdminChatMessageSearchRequest
import com.mymentalcare.server.application.admin.request.AdminReportSearchRequest
import com.mymentalcare.server.bootstrap.admin.web.response.AdminChatMessageResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminReportDetailResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminReportEmotionPointResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminReportPageResponse
import com.mymentalcare.server.bootstrap.admin.web.response.AdminReportSongResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class AdminReportController(
    private val adminReportQueryInputPort: AdminReportQueryInputPort,
) {
    @Operation(summary = "관리자 리포트 목록 조회", description = "관리자가 회원, 날짜, 키워드로 마음 리포트를 조회합니다.")
    @GetMapping("/api/admin/reports")
    fun readReports(
        @RequestParam(required = false) memberId: Long?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminReportPageResponse {
        return adminReportQueryInputPort.readReports(
            AdminReportSearchRequest(
                memberId = memberId,
                date = date,
                keyword = keyword,
                page = page,
                size = size,
            ),
        ).toBootstrapResponse()
    }

    @Operation(summary = "관리자 리포트 상세 조회", description = "관리자가 마음 리포트의 요약, 감정 점수, 추천곡을 조회합니다.")
    @GetMapping("/api/admin/reports/{reportId}")
    fun readReport(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable reportId: Long,
    ): AdminReportDetailResponse {
        return adminReportQueryInputPort.readReport(adminMemberId = adminMemberId, reportId = reportId).toBootstrapResponse()
    }

    @Operation(summary = "관리자 원문 대화 조회", description = "관리자가 조회 사유를 입력한 경우에만 원문 대화를 조회합니다.")
    @GetMapping("/api/admin/chat-rooms/{roomId}/messages")
    fun readChatMessages(
        @AuthenticationPrincipal adminMemberId: Long,
        @PathVariable roomId: Long,
        @RequestParam(required = false) reason: String?,
    ): List<AdminChatMessageResponse> {
        return adminReportQueryInputPort.readChatMessages(
            AdminChatMessageSearchRequest(
                adminMemberId = adminMemberId,
                roomId = roomId,
                reason = reason,
            ),
        ).map { it.toBootstrapResponse() }
    }
}

private fun com.mymentalcare.server.application.admin.response.AdminReportPageResponse.toBootstrapResponse(): AdminReportPageResponse {
    return AdminReportPageResponse(
        reports = reports.map {
            com.mymentalcare.server.bootstrap.admin.web.response.AdminReportSummaryResponse(
                id = it.id,
                roomId = it.roomId,
                memberId = it.memberId,
                conversationDate = it.conversationDate,
                primaryEmotion = it.primaryEmotion,
                emotionScore = it.emotionScore,
                mainCause = it.mainCause,
                createdAt = it.createdAt,
            )
        },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminReportDetailResponse.toBootstrapResponse(): AdminReportDetailResponse {
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
        songs = songs.map { AdminReportSongResponse(title = it.title, artist = it.artist, reason = it.reason, youtubeUrl = it.youtubeUrl) },
        emotionTimeline = emotionTimeline.map { AdminReportEmotionPointResponse(label = it.label, score = it.score, reason = it.reason) },
        createdAt = createdAt,
    )
}

private fun com.mymentalcare.server.application.admin.response.AdminChatMessageResponse.toBootstrapResponse(): AdminChatMessageResponse {
    return AdminChatMessageResponse(
        id = id,
        roomId = roomId,
        senderType = senderType,
        content = content,
        messageOrder = messageOrder,
        createdAt = createdAt,
    )
}
