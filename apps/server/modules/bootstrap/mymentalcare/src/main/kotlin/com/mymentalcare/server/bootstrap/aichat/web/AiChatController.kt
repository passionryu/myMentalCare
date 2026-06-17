package com.mymentalcare.server.bootstrap.aichat.web

import com.mymentalcare.server.bootstrap.aichat.web.request.*
import com.mymentalcare.server.bootstrap.aichat.web.response.*
import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.application.aichat.port.AiChatInputPort
import com.mymentalcare.server.application.aichat.request.CreateAiChatReportRequest
import com.mymentalcare.server.application.aichat.request.DeleteAiChatHistoryRequest
import com.mymentalcare.server.application.aichat.request.SendAiChatMessageRequest
import com.mymentalcare.server.application.aichat.request.StartAiChatCheckInRequest
import com.mymentalcare.server.application.aichat.request.StartAiChatSegmentRequest
import com.mymentalcare.server.domain.aichat.AiChatCheckInAnswer
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
class AiChatController(
    private val aiChatInputPort: AiChatInputPort,
) {
    @Operation(
        summary = "오늘의 AI 마음 대화방 조회",
        description = "로그인한 사용자의 한국 시간 기준 오늘 대화방과 메시지를 조회합니다. 오늘 대화방이 없으면 새로 생성합니다.",
    )
    @GetMapping("/rooms/today")
    fun readTodayRoom(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<TodayAiChatRoomResponse> {
        val response = aiChatInputPort.readTodayRoom(memberId)

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "내 AI 마음 대화방 이력 목록 조회",
        description = "로그인한 사용자의 날짜별 대화방 목록과 최신 메시지 요약을 조회합니다.",
    )
    @GetMapping("/rooms")
    fun readHistoryRooms(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<List<AiChatHistoryRoomResponse>> {
        return ResponseEntity.ok(
            aiChatInputPort.readHistoryRooms(memberId).map { it.toBootstrapResponse() }
        )
    }

    @Operation(
        summary = "내 AI 마음 대화방 상세 조회",
        description = "로그인한 사용자의 특정 대화방 메시지 이력을 조회합니다.",
    )
    @GetMapping("/rooms/{roomId}")
    fun readHistoryRoom(
        @AuthenticationPrincipal memberId: Long,
        @PathVariable roomId: Long,
    ): ResponseEntity<AiChatHistoryRoomDetailResponse> {
        val response = aiChatInputPort.readHistoryRoom(memberId = memberId, roomId = roomId)

        return response?.let { ResponseEntity.ok(it.toBootstrapResponse()) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "내 마음 리포트 목록 조회",
        description = "로그인한 사용자의 저장된 마음 리포트 목록을 최신순으로 조회합니다.",
    )
    @GetMapping("/reports")
    fun readReports(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<List<AiChatReportResponse>> {
        return ResponseEntity.ok(
            aiChatInputPort.readReports(memberId).map { it.toBootstrapResponse() }
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
        val response = aiChatInputPort.readReport(memberId = memberId, reportId = reportId)

        return response?.let { ResponseEntity.ok(it.toBootstrapResponse()) }
            ?: ResponseEntity.notFound().build()
    }

    @Operation(
        summary = "내 마음 체크인 기록 조회",
        description = "로그인한 사용자의 체크인 기록과 답변을 최신순으로 조회합니다.",
    )
    @GetMapping("/check-ins")
    fun readCheckIns(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<List<AiChatCheckInHistoryResponse>> {
        return ResponseEntity.ok(
            aiChatInputPort.readCheckIns(memberId).map { it.toBootstrapResponse() }
        )
    }

    @Operation(
        summary = "내 AI 마음 이력 삭제",
        description = "로그인한 사용자의 채팅방, 리포트, 체크인 기록 중 선택한 이력을 삭제합니다.",
    )
    @PostMapping("/history/delete")
    fun deleteHistory(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody payload: DeleteAiChatHistoryPayload,
    ): ResponseEntity<DeleteAiChatHistoryResponse> {
        val response = aiChatInputPort.deleteHistory(
            memberId = memberId,
            request = DeleteAiChatHistoryRequest(
                targetType = payload.targetType,
                targetId = payload.targetId,
            ),
        )

        return ResponseEntity.ok(DeleteAiChatHistoryResponse(deletedCount = response.deletedCount))
    }

    @Operation(
        summary = "오늘 대화방 새 주제 시작",
        description = "체크인 없이 오늘 대화방 안에 새 주제 구간을 만들고 마음이의 첫 메시지를 반환합니다.",
    )
    @PostMapping("/rooms/today/segments")
    fun startSegment(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: StartAiChatSegmentPayload,
    ): ResponseEntity<StartAiChatSegmentResponse> {
        val response = aiChatInputPort.startSegment(
            memberId = memberId,
            request = StartAiChatSegmentRequest(
                startType = request.startType,
                clientRequestId = request.clientRequestId,
            ),
        )

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "체크인 기반 오늘 대화 구간 시작",
        description = "체크인 답변을 저장하고 오늘 대화방 안에 새 구간을 만든 뒤 체크인 맥락이 반영된 첫 메시지를 반환합니다.",
    )
    @PostMapping("/rooms/today/segments/check-in")
    fun startCheckInSegment(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: StartAiChatCheckInPayload,
    ): ResponseEntity<StartAiChatSegmentResponse> {
        val response = aiChatInputPort.startCheckInSegment(
            memberId = memberId,
            request = StartAiChatCheckInRequest(
                templateType = request.templateType.toCheckInTemplateType(),
                answers = request.answers.map { it.toApplicationAnswer() },
                clientRequestId = request.clientRequestId,
            ),
        )

        return ResponseEntity.ok(response.toBootstrapResponse())
    }

    @Operation(
        summary = "AI 마음 대화 메시지 전송",
        description = "사용자 메시지를 저장하고 기본 공감형 챗봇 응답을 생성합니다. 위기 키워드가 감지되면 안전 안내를 함께 반환합니다.",
    )
    @PostMapping("/rooms/today/messages")
    fun sendMessage(
        @AuthenticationPrincipal memberId: Long,
        @Valid @RequestBody request: SendAiChatMessagePayload,
    ): ResponseEntity<SendAiChatMessageResponse> {
        val response = aiChatInputPort.sendMessage(
            memberId = memberId,
            request = SendAiChatMessageRequest(
                content = request.content,
                segmentId = request.segmentId,
                clientRequestId = request.clientRequestId,
            ),
        )

        val bootstrapResponse = response.toBootstrapResponse()
        val status = if (response.aiReplyFailed) HttpStatus.SERVICE_UNAVAILABLE else HttpStatus.OK

        return ResponseEntity.status(status).body(bootstrapResponse)
    }

    @Operation(
        summary = "오늘 마음 리포트 생성 가능 여부 조회",
        description = "오늘 대화가 충분한지 판단해 바로 리포트를 만들지, 짧은 대화 안내 모달을 먼저 보여줄지 결정합니다.",
    )
    @GetMapping("/rooms/today/report-readiness")
    fun readTodayReportReadiness(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<AiChatReportReadinessResponse> {
        val response = aiChatInputPort.readTodayReportReadiness(memberId)

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
        val response = aiChatInputPort.createTodayReport(
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
        val response = aiChatInputPort.readLatestTodayReport(memberId)

        return response?.let { ResponseEntity.ok(it.toBootstrapResponse()) }
            ?: ResponseEntity.noContent().build()
    }

    private fun String.toCheckInTemplateType(): AiChatCheckInTemplateType {
        return runCatching { AiChatCheckInTemplateType.valueOf(this) }
            .getOrElse { throw AiChatInvalidRequestException("지원하지 않는 체크인 유형입니다.") }
    }

    private fun AiChatCheckInAnswerPayload.toApplicationAnswer(): AiChatCheckInAnswer {
        return AiChatCheckInAnswer(
            stepKey = stepKey,
            optionKey = optionKey,
            label = label,
            value = value,
            freeText = freeText,
        )
    }
}
