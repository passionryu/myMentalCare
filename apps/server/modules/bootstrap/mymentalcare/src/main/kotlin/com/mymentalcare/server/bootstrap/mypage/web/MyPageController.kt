package com.mymentalcare.server.bootstrap.mypage.web

import com.mymentalcare.server.bootstrap.mypage.web.response.*

import com.mymentalcare.server.application.mypage.port.MyPageInputPort
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mypage")
class MyPageController(
    private val myPageInputPort: MyPageInputPort,
) {
    @Operation(
        summary = "마이페이지 요약 조회",
        description = "로그인한 사용자의 최근 대화, 리포트, 알림 설정 상태를 요약해 조회합니다.",
    )
    @GetMapping("/summary")
    fun readSummary(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<MyPageSummaryResponse> {
        val response = myPageInputPort.readSummary(memberId)

        return ResponseEntity.ok(
            MyPageSummaryResponse(
                hasTodayChat = response.hasTodayChat,
                todayMessageCount = response.todayMessageCount,
                recentChatAt = response.recentChatAt,
                reportCount = response.reportCount,
                latestReportAt = response.latestReportAt,
                latestReportDate = response.latestReportDate,
                notificationEnabled = response.notificationEnabled,
                notificationTime = response.notificationTime,
            )
        )
    }
}
