package com.mymentalcare.server.bootstrap.mindstatistics.web

import com.mymentalcare.server.application.mindstatistics.port.MindStatisticsInputPort
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsCalendarResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsDayDetailResponse
import com.mymentalcare.server.bootstrap.mindstatistics.web.response.MindStatisticsOverviewResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.YearMonth

@RestController
@RequestMapping("/api/mind-statistics")
class MindStatisticsController(
    private val mindStatisticsInputPort: MindStatisticsInputPort,
) {
    @Operation(
        summary = "내 마음 통계 달력 조회",
        description = "로그인한 사용자의 월별 마음 대화 여부, 리포트 여부, 대표 감정을 달력 형태로 조회합니다.",
    )
    @GetMapping("/calendar")
    fun readCalendar(
        @AuthenticationPrincipal memberId: Long,
        @RequestParam month: String,
    ): ResponseEntity<MindStatisticsCalendarResponse> {
        val response = mindStatisticsInputPort.readCalendar(memberId, YearMonth.parse(month))

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "내 마음 통계 날짜 상세 조회",
        description = "로그인한 사용자의 특정 날짜 대화 미리보기와 저장된 마음 리포트를 조회합니다.",
    )
    @GetMapping("/days/{date}")
    fun readDayDetail(
        @AuthenticationPrincipal memberId: Long,
        @PathVariable date: LocalDate,
    ): ResponseEntity<MindStatisticsDayDetailResponse> {
        val response = mindStatisticsInputPort.readDayDetail(memberId, date)

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "내 마음 통계 요약 조회",
        description = "로그인한 사용자의 최근 1~4주 감정 분포와 주간 감정 흐름을 조회합니다.",
    )
    @GetMapping("/overview")
    fun readOverview(
        @AuthenticationPrincipal memberId: Long,
        @RequestParam(defaultValue = "4") rangeWeeks: Int,
    ): ResponseEntity<MindStatisticsOverviewResponse> {
        val response = mindStatisticsInputPort.readOverview(memberId, rangeWeeks)

        return ResponseEntity.ok(response.toBootstrapResponse())
    }
}
