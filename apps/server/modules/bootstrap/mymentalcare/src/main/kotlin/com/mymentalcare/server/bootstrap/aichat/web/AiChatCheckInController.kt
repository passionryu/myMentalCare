package com.mymentalcare.server.bootstrap.aichat.web

import com.mymentalcare.server.application.aichat.AiChatInvalidRequestException
import com.mymentalcare.server.application.aichat.port.AiChatCheckInInputPort
import com.mymentalcare.server.application.aichat.request.StartAiChatCheckInRequest
import com.mymentalcare.server.bootstrap.aichat.web.request.AiChatCheckInAnswerPayload
import com.mymentalcare.server.bootstrap.aichat.web.request.StartAiChatCheckInPayload
import com.mymentalcare.server.bootstrap.aichat.web.response.AiChatCheckInHistoryResponse
import com.mymentalcare.server.bootstrap.aichat.web.response.StartAiChatSegmentResponse
import com.mymentalcare.server.domain.aichat.AiChatCheckInAnswer
import com.mymentalcare.server.domain.aichat.AiChatCheckInTemplateType
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-chat")
class AiChatCheckInController(
    private val aiChatCheckInInputPort: AiChatCheckInInputPort,
) {
    @Operation(
        summary = "내 마음 체크인 기록 조회",
        description = "로그인한 사용자의 체크인 기록과 답변을 최신순으로 조회합니다.",
    )
    @GetMapping("/check-ins")
    fun readCheckIns(
        @AuthenticationPrincipal memberId: Long,
    ): ResponseEntity<List<AiChatCheckInHistoryResponse>> {
        return ResponseEntity.ok(
            aiChatCheckInInputPort.readCheckIns(memberId).map { it.toBootstrapResponse() }
        )
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
        val response = aiChatCheckInInputPort.startCheckInSegment(
            memberId = memberId,
            request = StartAiChatCheckInRequest(
                templateType = request.templateType.toCheckInTemplateType(),
                answers = request.answers.map { it.toApplicationAnswer() },
                clientRequestId = request.clientRequestId,
            ),
        )

        return ResponseEntity.ok(response.toBootstrapResponse())
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
