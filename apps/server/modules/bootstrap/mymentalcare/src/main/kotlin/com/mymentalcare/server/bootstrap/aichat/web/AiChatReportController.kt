package com.mymentalcare.server.bootstrap.aichat.web

import com.mymentalcare.server.application.aichat.port.AiChatReportInputPort
import com.mymentalcare.server.application.aichat.request.CreateAiChatReportRequest
import com.mymentalcare.server.bootstrap.aichat.web.request.CreateAiChatReportPayload
import com.mymentalcare.server.bootstrap.aichat.web.response.AiChatReportReadinessResponse
import com.mymentalcare.server.bootstrap.aichat.web.response.AiChatReportResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-chat")
class AiChatReportController(
    private val aiChatReportInputPort: AiChatReportInputPort,
) {
    @Operation(
        summary = "내 마음 리포트 목록 조회",
        description = "로그인한 사용자의 저장된 마음 리포트 목록을 최신순으로 조회합니다.",
    )
    @GetMapping("/reports")
    fun readReports(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<List<AiChatReportResponse>> {
        return ResponseEntity.ok(
            aiChatReportInputPort.readReports(memberId).map { it.toBootstrapResponse() }
        )
    }

    @Operation(
        summary = "내 마음 리포트 상세 조회",
        description = "로그인한 사용자의 특정 마음 리포트 상세와 추천 노래를 조회합니다.",
    )
    @GetMapping("/reports/{reportId}")
    fun readReport(
        @AuthenticationPrincipal memberId: Long,
        @PathVariable reportId: Long,
    ): ResponseEntity<AiChatReportResponse> {
        val response = aiChatReportInputPort.readReport(memberId = memberId, reportId = reportId)

        return response?.let { ResponseEntity.ok(it.toBootstrapResponse()) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "오늘 마음 리포트 생성 가능 여부 조회",
        description = "오늘 대화가 충분한지 판단해 바로 리포트를 만들지, 짧은 대화 안내 모달을 먼저 보여줄지 결정합니다.",
    )
    @GetMapping("/rooms/today/report-readiness")
    fun readTodayReportReadiness(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<AiChatReportReadinessResponse> {
        val response = aiChatReportInputPort.readTodayReportReadiness(memberId)

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "오늘 마음 리포트 생성",
        description = "오늘 대화를 FULL 또는 SHORT 리포트로 정리하고 생성 즉시 저장합니다.",
    )
    @PostMapping("/rooms/today/reports")
    fun createTodayReport(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: CreateAiChatReportPayload,
    ): ResponseEntity<AiChatReportResponse> {
        val response = aiChatReportInputPort.createTodayReport(
            memberId = memberId,
            request = CreateAiChatReportRequest(
                forceCreate = request.forceCreate,
                clientRequestId = request.clientRequestId,
            ),
        )

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "오늘 최신 마음 리포트 조회",
        description = "오늘 대화방에 저장된 최신 마음 리포트를 조회합니다. 아직 리포트가 없으면 빈 응답을 반환합니다.",
    )
    @GetMapping("/rooms/today/reports/latest")
    fun readLatestTodayReport(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<AiChatReportResponse> {
        val response = aiChatReportInputPort.readLatestTodayReport(memberId)

        return response?.let { ResponseEntity.ok(it.toBootstrapResponse()) }
            ?: ResponseEntity.noContent().build()
    }
}
